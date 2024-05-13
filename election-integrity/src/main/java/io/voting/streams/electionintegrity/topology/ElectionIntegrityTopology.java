package io.voting.streams.electionintegrity.topology;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionView;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.electionintegrity.model.ElectionHeartbeat;
import io.voting.streams.electionintegrity.model.ElectionSummary;
import io.voting.streams.electionintegrity.model.TTLSummary;
import io.voting.streams.electionintegrity.topology.aggregate.TTLAggregator;
import io.voting.streams.electionintegrity.topology.aggregate.VoteAggregator;
import io.voting.streams.electionintegrity.topology.joiner.ElectionViewJoiner;
import io.voting.streams.electionintegrity.topology.mappers.ElectionIdExtractor;
import io.voting.streams.electionintegrity.topology.mappers.ElectionMapper;
import io.voting.streams.electionintegrity.topology.mappers.ce.CEElectionMapper;
import io.voting.streams.electionintegrity.topology.mappers.ce.CETTLMapper;
import io.voting.streams.electionintegrity.topology.mappers.ce.CEVoteMapper;
import io.voting.streams.electionintegrity.topology.predicates.FirstVoteProcessor;
import io.voting.streams.electionintegrity.topology.predicates.IllegalContentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;
import java.util.Properties;

@Slf4j
public final class ElectionIntegrityTopology {

  public static final Serde<String> stringSerde = Serdes.String();
  public static final Serde<CloudEvent> ceSerde = StreamUtils.getCESerde();
  private static final Serde<ElectionView> electionViewSerde = StreamUtils.getJsonSerde(ElectionView.class);

  private static final Predicate<String, ElectionCreate> ILLEGAL_ELECTION_FILTER = new IllegalContentProcessor();
  private static final ValueMapper<Election, CloudEvent> LEGAL_ELECTION_CE_MAPPER = new CEElectionMapper();
  private static final Predicate<String, ElectionSummary> LEGAL_VOTE_FILTER = new FirstVoteProcessor();
  private static final Aggregator<String, ElectionVote, ElectionSummary> VOTE_AGGREGATOR = new VoteAggregator();
  private static final KeyValueMapper<String, ElectionSummary, String> VOTE_ELECTION_ID_EXTRACTOR = new ElectionIdExtractor();
  private static final ValueMapper<ElectionSummary, CloudEvent> LEGAL_VOTE_CE_MAPPER = new CEVoteMapper();

  private static final ValueMapper<TTLSummary, CloudEvent> TTL_CE_MAPPER = new CETTLMapper();
  private static final Aggregator<String, ElectionHeartbeat, TTLSummary> TTL_AGGREGATOR = new TTLAggregator();
  // if there is no record w/ the same key on the right side of the join, then the right value is set to null
  private static final ValueJoiner<CloudEvent, ElectionView, ElectionHeartbeat> ELECTION_VIEW_JOINER = new ElectionViewJoiner();

  private ElectionIntegrityTopology() {}

  public static Topology buildTopology(final StreamsBuilder builder, final Properties properties) {
    final String inputTopic = properties.getProperty("input.topic");

    final KStream<String, CloudEvent> commandStream = builder
            .stream(inputTopic, Consumed.with(stringSerde, ceSerde));

    final KStream<String, CloudEvent> electionCommands = commandStream.filter((k, ce) -> CloudEventTypes.ELECTION_CREATE_CMD.equals(ce.getType()));
    final KStream<String, CloudEvent> voteCommands = commandStream
            .filter((k, ce) -> CloudEventTypes.ELECTION_VOTE_CMD.equals(ce.getType()));
    final KStream<String, CloudEvent> viewCommands = commandStream
            .filter((k, ce) -> CloudEventTypes.ELECTION_VIEW_CMD.equals(ce.getType()));

    final KStream<String, CloudEvent> legalElectionStream = defineEIntegrity(properties, electionCommands);
    final KStream<String, CloudEvent> legalVoteStream = defineVIntegrity(properties, voteCommands);
    final KTable<String, ElectionView> electionViewState = defineElectionViewState(viewCommands);
    defineElectionTTL(properties, legalElectionStream, legalVoteStream, viewCommands, electionViewState);

    final Topology topology = builder.build();
    log.debug("=== Topology === \n{}", topology.describe());
    return topology;
  }

  private static KTable<String, ElectionView> defineElectionViewState(KStream<String, CloudEvent> viewCommands) {
    return viewCommands
            .mapValues(ce -> StreamUtils.unwrapCloudEventData(ce.getData(), ElectionView.class))
            .toTable(Named.as("election.view.table"), Materialized.<String, ElectionView, KeyValueStore<Bytes, byte[]>>as("election.views").withKeySerde(stringSerde).withValueSerde(StreamUtils.getJsonSerde(ElectionView.class)));
  }

  private static KStream<String, CloudEvent> defineEIntegrity(final Properties properties, final KStream<String, CloudEvent> electionCommands) {
    final String electionOutputTopic = properties.getProperty("output.topic.elections");
    final ValueMapper<ElectionCreate, Election> electionEnrichment = new ElectionMapper(properties.getProperty("election.ttl"));
    final KStream<String, CloudEvent> legalElectionStream = electionCommands
            .mapValues(ce -> StreamUtils.unwrapCloudEventData(ce.getData(), ElectionCreate.class))
            .filterNot(ILLEGAL_ELECTION_FILTER)
            .mapValues(electionEnrichment)
            .mapValues(LEGAL_ELECTION_CE_MAPPER)
            .selectKey((k, v) -> v.getId());
    legalElectionStream
            .to(electionOutputTopic, Produced.with(stringSerde, ceSerde));
    return legalElectionStream;
  }

  private static KStream<String, CloudEvent> defineVIntegrity(final Properties properties, final KStream<String, CloudEvent> voteCommands) {
    final String votesOutputTopic = properties.getProperty("output.topic.votes");
    final KStream<String, CloudEvent> legalVoteStream = voteCommands
            .mapValues(ce -> StreamUtils.unwrapCloudEventData(ce.getData(), ElectionVote.class))
            .groupByKey()
            .aggregate(
                    VoteAggregator.initializer(),
                    VOTE_AGGREGATOR,
                    VoteAggregator.materialize()
            )
            .toStream()
            .filter(LEGAL_VOTE_FILTER)
            .selectKey(VOTE_ELECTION_ID_EXTRACTOR)
            .mapValues(LEGAL_VOTE_CE_MAPPER);
    legalVoteStream
            .to(votesOutputTopic, Produced.with(stringSerde, ceSerde));
    return legalVoteStream;
  }

  public static void defineElectionTTL(final Properties properties,
                                       final KStream<String, CloudEvent> legalElectionStream,
                                       final KStream<String, CloudEvent> legalVoteStream,
                                       final KStream<String, CloudEvent> viewCommands,
                                       final KTable<String, ElectionView> electionViewState) {
    final String electionTTLDuration = properties.getProperty("election.ttl");
    final String electionOutputTopic = properties.getProperty("output.topic.elections");
    final Duration windowDuration = Duration.parse(electionTTLDuration);
    legalElectionStream
            .merge(viewCommands)
            .merge(legalVoteStream, Named.as("election.ttl.merge"))
            .leftJoin(electionViewState, ELECTION_VIEW_JOINER, Joined.with(stringSerde, ceSerde, electionViewSerde))
            .filter((eId, hb) -> hb.view() != ElectionView.CLOSED)
            .groupByKey()
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMillis(windowDuration.toMillis())))
            .aggregate(
                    TTLAggregator.initializer(),
                    TTL_AGGREGATOR,
                    TTLAggregator.materialize()
            )
            .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().shutDownWhenFull()).withName("election.ttl.suppress"))
            .toStream()
            .map((windowedKey, ttlSummary) -> new KeyValue<>(windowedKey.key(), TTL_CE_MAPPER.apply(ttlSummary)))
            .to(electionOutputTopic, Produced.with(stringSerde, ceSerde));
  }

}
