package io.voting.streams.electionintegrity.topology;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionView;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.electionintegrity.framework.TestCEBuilder;
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
import java.util.Arrays;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

class TTLTopologyTest {
  static final String ELECTION_COMMANDS = "election.commands";
  static final String ELECTION_REQUESTS = "election.requests";
  static final String ELECTION_VOTES = "election.votes";

  static final Serde<String> STRING_SERDE = Serdes.String();
  static final Serde<CloudEvent> CLOUD_EVENT_SERDE = StreamUtils.getCESerde();

  static Topology topology;
  static Properties properties;
  TopologyTestDriver testDriver;
  TestInputTopic<String, CloudEvent> inputTopic;
  TestOutputTopic<String, CloudEvent> outputTopic;

  @BeforeAll
  static void build() {
    properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "election-ttl.test");
    properties.put("election.ttl", "PT15M");
    properties.put("input.topic", ELECTION_COMMANDS);
    properties.put("output.topic.elections", ELECTION_REQUESTS);
    properties.put("output.topic.votes", ELECTION_VOTES);


    final StreamsBuilder builder = new StreamsBuilder();
    topology = ElectionIntegrityTopology.buildTopology(builder, properties);
  }

  @AfterEach
  void cleanup() {
    testDriver.close();
  }

  @BeforeEach
  void setup() {
    testDriver = new TopologyTestDriver(topology, properties);
    inputTopic = testDriver.createInputTopic(ELECTION_COMMANDS, STRING_SERDE.serializer(), CLOUD_EVENT_SERDE.serializer());
    outputTopic = testDriver.createOutputTopic(ELECTION_REQUESTS, STRING_SERDE.deserializer(), CLOUD_EVENT_SERDE.deserializer());
  }

  @Test
  void test() {
    final ElectionCreate election = new ElectionCreate("testAuthor", "testTitle", "test desc", "TEST", Arrays.asList("Foo", "Bar"));
    inputTopic.pipeInput("userId0", TestCEBuilder.buildCE(election));
    final KeyValue<String, CloudEvent> legalElection = outputTopic.readKeyValue();
    assertThat(legalElection).isNotNull();

    inputTopic.pipeInput(legalElection.key, TestCEBuilder.buildCE(ElectionView.OPEN));
    final CloudEvent voteEvent = TestCEBuilder.buildCE(new ElectionVote(legalElection.key, "Foo"));
    inputTopic.pipeInput("userId0:" + legalElection.key, voteEvent);
    inputTopic.pipeInput("userId1:" + legalElection.key, voteEvent);
    inputTopic.pipeInput("userId2:" + legalElection.key, voteEvent);

    assertThat(outputTopic.getQueueSize()).isZero();
    inputTopic.advanceTime(Duration.ofMinutes(15L));
    assertThat(outputTopic.getQueueSize()).isZero();


    inputTopic.pipeInput(legalElection.key, TestCEBuilder.buildCE(ElectionView.PENDING));
    assertThat(outputTopic.getQueueSize()).isOne();
    final KeyValue<String, CloudEvent> actualTTLEvent = outputTopic.readKeyValue();
    assertThat(actualTTLEvent.key).isEqualTo(legalElection.key);
    assertThat(new String(actualTTLEvent.value.getData().toBytes())).isEqualTo(legalElection.key);
    assertThat(actualTTLEvent.value.getType()).isEqualTo(CloudEventTypes.ELECTION_EXPIRATION_EVENT);
  }

}
