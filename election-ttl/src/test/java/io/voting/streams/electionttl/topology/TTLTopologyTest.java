package io.voting.streams.electionttl.topology;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.streams.electionttl.ElectionTTLConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class TTLTopologyTest {

  final String ELECTION_ID = "eid123";

  static final String ELECTION_REQUESTS = "election.requests";
  static final String ELECTION_VOTES = "election.votes";
  static final String ELECTION_VOTES_CACHE = "vote.integrity.001-votes.integrity.aggregate-changelog";

  static final Serde<String> STRING_SERDE = Serdes.String();
  static final Serde<CloudEvent> CLOUD_EVENT_SERDE = StreamUtils.getCESerde();

  static Topology topology;
  static Properties properties;
  TopologyTestDriver testDriver;
  TestInputTopic<String, CloudEvent> electionRequestsInput;
  TestInputTopic<String, CloudEvent> electionVotesInput;
  TestOutputTopic<String, CloudEvent> electionTTLOutput;
  TestOutputTopic<String, String> tombstoneVotesOutput;

  @BeforeAll
  static void build() {
    properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "election-ttl.test");
    properties.put(ElectionTTLConfig.ELECTION_TTL, "PT15M");
    properties.put(ElectionTTLConfig.ELECTION_REQUESTS_TOPIC, ELECTION_REQUESTS);
    properties.put(ElectionTTLConfig.ELECTION_VOTES_TOPIC, ELECTION_VOTES);
    properties.put(ElectionTTLConfig.VOTE_INTEGRITY_CHANGELOG_TOPIC, ELECTION_VOTES_CACHE);


    final StreamsBuilder builder = new StreamsBuilder();
    topology = TTLTopology.buildTopology(builder, properties);
  }

  @BeforeEach
  void setup() {
    testDriver = new TopologyTestDriver(topology, properties);
    electionRequestsInput = testDriver.createInputTopic(ELECTION_REQUESTS, STRING_SERDE.serializer(), CLOUD_EVENT_SERDE.serializer());
    electionVotesInput = testDriver.createInputTopic(ELECTION_VOTES, STRING_SERDE.serializer(), CLOUD_EVENT_SERDE.serializer());
    electionTTLOutput = testDriver.createOutputTopic(ELECTION_REQUESTS, STRING_SERDE.deserializer(), CLOUD_EVENT_SERDE.deserializer());
    tombstoneVotesOutput = testDriver.createOutputTopic(ELECTION_VOTES_CACHE, STRING_SERDE.deserializer(), STRING_SERDE.deserializer());
  }

  @AfterEach
  void cleanup() { testDriver.close(); }

  @Test
  void testAggregationSuppression() {
    electionRequestsInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionCreate(ELECTION_ID));
    electionVotesInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionVote("001", ELECTION_ID));
    electionVotesInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionVote("002", ELECTION_ID));

    assertThat(electionTTLOutput.isEmpty()).isTrue();
    assertThat(tombstoneVotesOutput.isEmpty()).isTrue();

  }

  @Test
  void testOutboundEventsPostSuppression() {
    electionRequestsInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionCreate(ELECTION_ID));
    electionVotesInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionVote("001", ELECTION_ID));
    electionVotesInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionVote("002", ELECTION_ID));
    electionVotesInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionVote("003", ELECTION_ID));
    electionVotesInput.advanceTime(Duration.ofMinutes(15));
    electionVotesInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionVote("-1", ELECTION_ID));

    final List<KeyValue<String, CloudEvent>> actualTTLEvents = electionTTLOutput.readKeyValuesToList();
    assertThat(actualTTLEvents)
            .isNotEmpty()
            .hasSize(1)
            .containsOnly(
                    KeyValue.pair(ELECTION_ID, TestCEMapper.buildExpectedTTLEvent(ELECTION_ID))
            );

    final List<KeyValue<String, String>> actualTombstoneEvents = tombstoneVotesOutput.readKeyValuesToList();
    assertThat(actualTombstoneEvents).isNotEmpty().hasSize(3)
            .containsOnly(
                    KeyValue.pair("001" + ":" + ELECTION_ID, null),
                    KeyValue.pair("002" + ":" + ELECTION_ID, null),
                    KeyValue.pair("003" + ":" + ELECTION_ID, null)
            );
  }

  @Test
  void testLateArrivingEventsAreNotIncluded() {
    electionRequestsInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionCreate(ELECTION_ID));
    electionVotesInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionVote("001", ELECTION_ID));
    electionVotesInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionVote("002", ELECTION_ID));

    electionVotesInput.advanceTime(Duration.ofMinutes(16L));
    electionVotesInput.pipeInput(ELECTION_ID, TestCEMapper.buildElectionVote("003", ELECTION_ID));

    final List<KeyValue<String, CloudEvent>> actualTTLEvents = electionTTLOutput.readKeyValuesToList();
    assertThat(actualTTLEvents)
            .isNotEmpty()
            .hasSize(1)
            .containsOnly(
                    KeyValue.pair(ELECTION_ID, TestCEMapper.buildExpectedTTLEvent(ELECTION_ID))
            );

    final List<KeyValue<String, String>> actualTombstoneEvents = tombstoneVotesOutput.readKeyValuesToList();
    assertThat(actualTombstoneEvents).isNotEmpty().hasSize(2)
            .containsOnly(
                    KeyValue.pair("001" + ":" + ELECTION_ID, null),
                    KeyValue.pair("002" + ":" + ELECTION_ID, null)
            );
  }



}