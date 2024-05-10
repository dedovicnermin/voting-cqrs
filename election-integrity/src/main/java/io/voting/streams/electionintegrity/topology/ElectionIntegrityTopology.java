package io.voting.streams.electionintegrity.topology;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.electionintegrity.model.ElectionSummary;
import io.voting.streams.electionintegrity.topology.aggregate.VoteAggregator;
import io.voting.streams.electionintegrity.topology.mappers.ElectionIdExtractor;
import io.voting.streams.electionintegrity.topology.mappers.ElectionMapper;
import io.voting.streams.electionintegrity.topology.mappers.ce.CEElectionMapper;
import io.voting.streams.electionintegrity.topology.mappers.ce.CEVoteMapper;
import io.voting.streams.electionintegrity.topology.predicates.FirstVoteProcessor;
import io.voting.streams.electionintegrity.topology.predicates.IllegalContentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.util.Properties;

@Slf4j
public final class ElectionIntegrityTopology {

  private static final Predicate<String, ElectionCreate> illegalContent = new IllegalContentProcessor();
  private static final ValueMapper<Election, CloudEvent> cloudEventEnrichment = new CEElectionMapper();
  private static final Predicate<String, ElectionSummary> firstVoteProcessor = new FirstVoteProcessor();
  private static final Aggregator<String, ElectionVote, ElectionSummary> aggregator = new VoteAggregator();
  private static final KeyValueMapper<String, ElectionSummary, String> electionIdExtractor = new ElectionIdExtractor();
  private static final ValueMapper<ElectionSummary, CloudEvent> cloudEventMapper = new CEVoteMapper();

  private ElectionIntegrityTopology() {}

  public static Topology buildTopology(final StreamsBuilder builder, final Properties properties) {
    final String inputTopic = properties.getProperty("input.topic");

    final KStream<String, CloudEvent> commandStream = builder
            .stream(inputTopic, Consumed.with(Serdes.String(), StreamUtils.getCESerde()));

    final KStream<String, CloudEvent> electionCommands = commandStream
            .filter((k, ce) -> CloudEventTypes.ELECTION_CREATE_CMD.equals(ce.getType()));
    final KStream<String, CloudEvent> voteCommands = commandStream
            .filter((k, ce) -> CloudEventTypes.ELECTION_VOTE_CMD.equals(ce.getType()));

    defineEIntegrity(properties, electionCommands);
    defineVIntegrity(properties, voteCommands);

    final Topology topology = builder.build();
    log.debug("=== Topology === \n{}", topology.describe());
    return topology;
  }

  private static void defineEIntegrity(final Properties properties, final KStream<String, CloudEvent> electionCommands) {
    final String electionOutputTopic = properties.getProperty("output.topic.elections");
    final ValueMapper<ElectionCreate, Election> electionEnrichment = new ElectionMapper(properties.getProperty("election.ttl"));
    electionCommands
            .mapValues(ce -> StreamUtils.unwrapCloudEventData(ce.getData(), ElectionCreate.class))
            .filterNot(illegalContent)
            .mapValues(electionEnrichment)
            .mapValues(cloudEventEnrichment)
            .selectKey((k, v) -> v.getId())
            .to(electionOutputTopic, Produced.with(Serdes.String(), StreamUtils.getCESerde()));
  }

  private static void defineVIntegrity(final Properties properties, final KStream<String, CloudEvent> voteCommands) {
    final String votesOutputTopic = properties.getProperty("output.topic.votes");
    voteCommands
            .mapValues(ce -> StreamUtils.unwrapCloudEventData(ce.getData(), ElectionVote.class))
            .groupByKey()
            .aggregate(
                    VoteAggregator.initializer(),
                    aggregator,
                    VoteAggregator.materialize()
            )
            .toStream()
            .filter(firstVoteProcessor)
            .selectKey(electionIdExtractor)
            .mapValues(cloudEventMapper)
            .to(votesOutputTopic, Produced.with(Serdes.String(), StreamUtils.getCESerde()));
  }

}
