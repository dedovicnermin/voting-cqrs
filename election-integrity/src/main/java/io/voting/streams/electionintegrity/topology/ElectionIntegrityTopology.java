package io.voting.streams.electionintegrity.topology;

import io.cloudevents.CloudEvent;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionVote;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.CreateElection;
import io.voting.events.cmd.RegisterVote;
import io.voting.events.cmd.ViewElection;
import io.voting.events.integrity.NewElection;
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
  private static final String INPUT_TOPIC_CONFIG = "input.topic";
  private static final String OUTPUT_TOPIC_CONFIG = "output.topic";
  private static final String ELECTION_TTL_CONFIG = "election.ttl";


  private static final CmdTypeFilter CMD_ELECTION_FILTER = new ElectionCmdFilter();
  private static final CmdTypeFilter CMD_VOTE_FILTER = new VoteCmdFilter();
  private static final CmdTypeFilter CMD_VIEW_FILTER = new ViewCmdFilter();

  private static final Predicate<String, CreateElection> ILLEGAL_ELECTION_FILTER = new IllegalContentProcessor();
  private static final ValueMapper<NewElection, CloudEvent> LEGAL_ELECTION_CE_MAPPER = new CEElectionMapper();

  private static final Predicate<String, ElectionSummary> LEGAL_VOTE_FILTER = new FirstVoteProcessor();
  private static final Aggregator<String, ElectionVote, ElectionSummary> VOTE_AGGREGATOR = new VoteAggregator();
  private static final KeyValueMapper<String, ElectionSummary, String> VOTE_ELECTION_ID_EXTRACTOR = new ElectionIdExtractor();
  private static final ValueMapper<ElectionSummary, CloudEvent> LEGAL_VOTE_CE_MAPPER = new CEVoteMapper();

  private static final ValueMapper<ViewElection, CloudEvent> TTL_CE_MAPPER = new CETTLMapper();
  private static final Predicate<String, ViewElection> PENDING_ELECTION_FILTER = new PendingElectionFilter();

  private ElectionIntegrityTopology() {}

  public static Topology buildTopology(final StreamsBuilder builder, final Properties properties) {
    final Serde<Object> avroCESerde = StreamUtils.getAvroCESerde(properties);
    return getTopology(builder, properties, avroCESerde);
  }

  public static Topology buildTopology(final StreamsBuilder builder, final Properties properties, final SchemaRegistryClient client) {
    final Serde<Object> avroCESerde = StreamUtils.getAvroCESerde(client, properties);
    return getTopology(builder, properties, avroCESerde);
  }


  private static Topology getTopology(StreamsBuilder builder, Properties properties, Serde<Object> avroCESerde) {
    final Produced<String, Object> producedAvroCE = Produced.with(Serdes.String(), avroCESerde);

    final String inputTopic = properties.getProperty(INPUT_TOPIC_CONFIG);
    final KStream<String, CloudEvent> commandStream = builder
            .stream(inputTopic, Consumed.with(Serdes.String(), avroCESerde).withName("election.cmd.src"))
            .mapValues(CloudEvent.class::cast, Named.as("global.ce.upcast"));


    final KStream<String, CloudEvent> electionCommands = commandStream.filter(CMD_ELECTION_FILTER, CMD_ELECTION_FILTER.name());
    final KStream<String, CloudEvent> voteCommands = commandStream.filter(CMD_VOTE_FILTER, CMD_VOTE_FILTER.name());
    final KStream<String, CloudEvent> viewCommands = commandStream.filter(CMD_VIEW_FILTER, CMD_VIEW_FILTER.name());

    defineEIntegrity(properties, electionCommands, producedAvroCE);
    defineVIntegrity(properties, voteCommands, producedAvroCE);
    defineElectionTTL(properties, viewCommands, producedAvroCE);

    final Topology topology = builder.build();
    log.debug("=== Topology === \n{}", topology.describe());
    return topology;
  }


  private static void defineEIntegrity(final Properties properties, final KStream<String, CloudEvent> electionCommands, final Produced<String, Object> producedAvroCE) {
    final String electionOutputTopic = properties.getProperty(OUTPUT_TOPIC_CONFIG);
    final ValueMapper<CreateElection, NewElection> electionEnrichment = new ElectionMapper(properties.getProperty(ELECTION_TTL_CONFIG));
    electionCommands
            .mapValues(ce -> (CreateElection) AvroCloudEventData.<CmdEvent>dataOf(ce.getData()).getCmd(), Named.as("ei.unwrap.ce.data"))
            .filterNot(ILLEGAL_ELECTION_FILTER, IllegalContentProcessor.name())
            .mapValues(electionEnrichment, ElectionMapper.name())
            .mapValues(LEGAL_ELECTION_CE_MAPPER, CEElectionMapper.name())
            .selectKey((k, v) -> v.getId(), Named.as("ei.key.selector"))
            .mapValues(v -> (Object) v, Named.as("ei.object.upcast"))
            .to(electionOutputTopic, producedAvroCE.withName("ei.sink"));

  }

  private static void defineVIntegrity(final Properties properties, final KStream<String, CloudEvent> voteCommands, final Produced<String, Object> producedAvroCE) {
    final String votesOutputTopic = properties.getProperty(OUTPUT_TOPIC_CONFIG);
    voteCommands
            .mapValues(ce -> (RegisterVote) AvroCloudEventData.<CmdEvent>dataOf(ce.getData()).getCmd(), Named.as("vi.unwrap.ce.data"))
            .mapValues(rv -> ElectionVote.of(rv.getEId().toString(), rv.getVotedFor().toString()), Named.as("vi.dao.ce.data"))
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
            .mapValues(v -> (Object) v, Named.as("vi.object.upcast"))
            .to(votesOutputTopic, producedAvroCE.withName("vi.sink"));
  }

  private static void defineElectionTTL(final Properties properties, final KStream<String, CloudEvent> viewCommands, final Produced<String, Object> producedAvroCE) {
    final String electionOutputTopic = properties.getProperty(OUTPUT_TOPIC_CONFIG);
    viewCommands
            .mapValues(ce -> (ViewElection) AvroCloudEventData.<CmdEvent>dataOf(ce.getData()).getCmd(), Named.as("ttl.unwrap.ce.data"))
            .filter(PENDING_ELECTION_FILTER, PendingElectionFilter.name())
            .mapValues(TTL_CE_MAPPER, CETTLMapper.name())
            .mapValues(v -> (Object) v, Named.as("ttl.object.upcast"))
            .to(electionOutputTopic, producedAvroCE.withName("ttl.sink"));
  }

}
