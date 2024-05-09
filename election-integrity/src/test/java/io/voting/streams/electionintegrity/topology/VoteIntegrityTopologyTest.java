package io.voting.streams.electionintegrity.topology;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.electionintegrity.framework.TestCmdBuilder;
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

import java.util.Arrays;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class VoteIntegrityTopologyTest {

  static Topology topology;
  TopologyTestDriver testDriver;
  TestInputTopic<String, CloudEvent> inputTopic;
  TestOutputTopic<String, CloudEvent> outputTopic;
  static Properties properties;

  @BeforeAll
  static void buildTopology() {
    properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "voting.test");
    properties.put("input.topic", "input");
    properties.put("output.topic.elections", "output_elections");
    properties.put("output.topic.votes", "output_votes");
    properties.put("election.ttl", "P2D");

    final StreamsBuilder streamsBuilder = new StreamsBuilder();
    topology = ElectionIntegrityTopology.buildTopology(streamsBuilder, properties);
  }

  @BeforeEach
  void setup() {
    testDriver = new TopologyTestDriver(topology, properties);
    final Serde<CloudEvent> ceSerde = StreamUtils.getCESerde();
    final Serde<String> stringSerde = Serdes.String();
    inputTopic = testDriver.createInputTopic(properties.getProperty("input.topic"), stringSerde.serializer(), ceSerde.serializer());
    outputTopic = testDriver.createOutputTopic(properties.getProperty("output.topic.votes"), stringSerde.deserializer(), ceSerde.deserializer());
  }


  @AfterEach
  void cleanup() {
    testDriver.close();
  }

  @Test
  void test() {
    final String USER = "nerm";
    final String ELECTION = "001";
    final ElectionVote v1 = new ElectionVote(ELECTION, "Bob");
    final ElectionVote v2 = new ElectionVote(ELECTION, "Jack");
    final ElectionVote v3 = new ElectionVote(ELECTION, "Nancy");
    final ElectionVote v4 = new ElectionVote(ELECTION, "Jack");

    final String EVENT_KEY = USER + ":" + ELECTION;
    inputTopic.pipeKeyValueList(
            Arrays.asList(
                    new KeyValue<>(EVENT_KEY, TestCmdBuilder.buildCE(v1)),
                    new KeyValue<>(EVENT_KEY, TestCmdBuilder.buildCE(v1)),
                    new KeyValue<>(EVENT_KEY, TestCmdBuilder.buildCE(v1)),
                    new KeyValue<>(EVENT_KEY, TestCmdBuilder.buildCE(v2)),
                    new KeyValue<>(EVENT_KEY, TestCmdBuilder.buildCE(v3)),
                    new KeyValue<>(EVENT_KEY, TestCmdBuilder.buildCE(v4))
            )
    );
    assertThat(outputTopic.getQueueSize()).isOne();
    final KeyValue<String, CloudEvent> outputEvent = outputTopic.readKeyValue();
    assertThat(outputEvent.key).isEqualTo(ELECTION);
    assertThat(outputEvent.value.getSubject()).isEqualTo(ELECTION);
    assertThat(outputEvent.value.getData()).isNotNull();
    final ElectionVote actualEvent = StreamUtils.unwrapCloudEventData(outputEvent.value.getData(), ElectionVote.class);

    System.out.println();
    System.out.println(outputEvent);
    System.out.println(actualEvent);
    System.out.println();

    assertThat(actualEvent).isEqualTo(ElectionVote.of(ELECTION, "Bob"));

  }

}