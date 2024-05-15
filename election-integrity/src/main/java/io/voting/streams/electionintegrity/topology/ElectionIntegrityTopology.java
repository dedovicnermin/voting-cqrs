package io.voting.streams.electionintegrity.topology;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.electionintegrity.model.ElectionSummary;
import io.voting.streams.electionintegrity.topology.aggregate.VoteAggregator;
import io.voting.streams.electionintegrity.topology.mappers.ElectionIdExtractor;
import io.voting.streams.electionintegrity.topology.mappers.ElectionMapper;
import io.voting.streams.electionintegrity.topology.mappers.ce.CEElectionMapper;
import io.voting.streams.electionintegrity.topology.mappers.ce.CETTLMapper;
import io.voting.streams.electionintegrity.topology.mappers.ce.CEVoteMapper;
import io.voting.streams.electionintegrity.topology.predicates.FirstVoteProcessor;
import io.voting.streams.electionintegrity.topology.predicates.IllegalContentProcessor;
import io.voting.streams.electionintegrity.topology.predicates.PendingElectionFilter;
import io.voting.streams.electionintegrity.topology.predicates.cmd.CmdTypeFilter;
import io.voting.streams.electionintegrity.topology.predicates.cmd.ElectionCmdFilter;
import io.voting.streams.electionintegrity.topology.predicates.cmd.ViewCmdFilter;
import io.voting.streams.electionintegrity.topology.predicates.cmd.VoteCmdFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.util.Properties;

@Slf4j
public final class ElectionIntegrityTopology {

  public static final Serde<String> stringSerde = Serdes.String();
  public static final Serde<CloudEvent> ceSerde = StreamUtils.getCESerde();
  private static final Produced<String, CloudEvent> PRODUCED_WITH_STR_CE = Produced.with(stringSerde, ceSerde);

  private static final CmdTypeFilter CMD_ELECTION_FILTER = new ElectionCmdFilter();
  private static final CmdTypeFilter CMD_VOTE_FILTER = new VoteCmdFilter();
  private static final CmdTypeFilter CMD_VIEW_FILTER = new ViewCmdFilter();

  private static final Predicate<String, ElectionCreate> ILLEGAL_ELECTION_FILTER = new IllegalContentProcessor();
  private static final ValueMapper<Election, CloudEvent> LEGAL_ELECTION_CE_MAPPER = new CEElectionMapper();

  private static final Predicate<String, ElectionSummary> LEGAL_VOTE_FILTER = new FirstVoteProcessor();
  private static final Aggregator<String, ElectionVote, ElectionSummary> VOTE_AGGREGATOR = new VoteAggregator();
  private static final KeyValueMapper<String, ElectionSummary, String> VOTE_ELECTION_ID_EXTRACTOR = new ElectionIdExtractor();
  private static final ValueMapper<ElectionSummary, CloudEvent> LEGAL_VOTE_CE_MAPPER = new CEVoteMapper();

  private static final ValueMapper<CloudEvent, CloudEvent> TTL_CE_MAPPER = new CETTLMapper();
  private static final Predicate<String, CloudEvent> PENDING_ELECTION_FILTER = new PendingElectionFilter();

  private ElectionIntegrityTopology() {}

  public static Topology buildTopology(final StreamsBuilder builder, final Properties properties) {
    final String inputTopic = properties.getProperty("input.topic");
    final KStream<String, CloudEvent> commandStream = builder
            .stream(inputTopic, Consumed.with(stringSerde, ceSerde).withName("election.cmd.src"));

    final KStream<String, CloudEvent> electionCommands = commandStream.filter(CMD_ELECTION_FILTER, CMD_ELECTION_FILTER.name());
    final KStream<String, CloudEvent> voteCommands = commandStream.filter(CMD_VOTE_FILTER, CMD_VOTE_FILTER.name());
    final KStream<String, CloudEvent> viewCommands = commandStream.filter(CMD_VIEW_FILTER, CMD_VIEW_FILTER.name());

    defineEIntegrity(properties, electionCommands);
    defineVIntegrity(properties, voteCommands);
    defineElectionTTL(properties, viewCommands);

    final Topology topology = builder.build();
    log.debug("=== Topology === \n{}", topology.describe());
    return topology;
  }


  private static void defineEIntegrity(final Properties properties, final KStream<String, CloudEvent> electionCommands) {
    final String electionOutputTopic = properties.getProperty("output.topic.elections");
    final ValueMapper<ElectionCreate, Election> electionEnrichment = new ElectionMapper(properties.getProperty("election.ttl"));
    electionCommands
            .mapValues(ce -> StreamUtils.unwrapCloudEventData(ce.getData(), ElectionCreate.class), Named.as("ei.unwrap.ce.data"))
            .filterNot(ILLEGAL_ELECTION_FILTER, IllegalContentProcessor.name())
            .mapValues(electionEnrichment, ElectionMapper.name())
            .mapValues(LEGAL_ELECTION_CE_MAPPER, CEElectionMapper.name())
            .selectKey((k, v) -> v.getId(), Named.as("ei.key.selector"))
            .to(electionOutputTopic, PRODUCED_WITH_STR_CE.withName("ei.sink"));

  }

  private static void defineVIntegrity(final Properties properties, final KStream<String, CloudEvent> voteCommands) {
    final String votesOutputTopic = properties.getProperty("output.topic.votes");
    voteCommands
            .mapValues(ce -> StreamUtils.unwrapCloudEventData(ce.getData(), ElectionVote.class), Named.as("vi.unwrap.ce.data"))
            .groupByKey()
            .aggregate(
                    VoteAggregator.initializer(),
                    VOTE_AGGREGATOR,
                    VoteAggregator.name(),
                    VoteAggregator.materialize()
            )
            .toStream(Named.as("vi.integrity.aggregate.stream"))
            .filter(LEGAL_VOTE_FILTER, FirstVoteProcessor.name())
            .selectKey(VOTE_ELECTION_ID_EXTRACTOR, ElectionIdExtractor.name())
            .mapValues(LEGAL_VOTE_CE_MAPPER, CEVoteMapper.name())
            .to(votesOutputTopic, PRODUCED_WITH_STR_CE.withName("vi.sink"));
  }

  private static void defineElectionTTL(final Properties properties, final KStream<String, CloudEvent> viewCommands) {
    final String electionOutputTopic = properties.getProperty("output.topic.elections");
    viewCommands
            .filter(PENDING_ELECTION_FILTER, PendingElectionFilter.name())
            .mapValues(TTL_CE_MAPPER, CETTLMapper.name())
            .to(electionOutputTopic, PRODUCED_WITH_STR_CE.withName("ttl.sink"));
  }

}
