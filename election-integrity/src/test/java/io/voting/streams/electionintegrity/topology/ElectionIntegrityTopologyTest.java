package io.voting.streams.electionintegrity.topology;

import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionStatus;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionIntegrityTopologyTest {

  static final String INPUT_TOPIC = "election.requests.raw";
  static final String OUTPUT_TOPIC = "election.requests";

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
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "election-integrity.test");
    properties.put("input.topic", INPUT_TOPIC);
    properties.put("output.topic.elections", OUTPUT_TOPIC);
    properties.put("output.topic.votes", "election.votes");
    properties.put("election.ttl", "P2D");

    final StreamsBuilder builder = new StreamsBuilder();
    topology = ElectionIntegrityTopology.buildTopology(builder, properties);
  }

  @BeforeEach
  void setup() {
    testDriver = new TopologyTestDriver(topology, properties);
    inputTopic = testDriver.createInputTopic(INPUT_TOPIC, STRING_SERDE.serializer(), CLOUD_EVENT_SERDE.serializer());
    outputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC, STRING_SERDE.deserializer(), CLOUD_EVENT_SERDE.deserializer());
  }

  @AfterEach
  void cleanup() { testDriver.close(); }

  @Test
  void test() {
    final Faker fake = Faker.instance();
    final ElectionCreate legalElection = new ElectionCreate(fake.funnyName().name(), "Good Election", fake.lorem().paragraph(), "GOOD", Arrays.asList(fake.funnyName().name(), fake.funnyName().name()));
    final ElectionCreate illegalElection = new ElectionCreate(fake.funnyName().name(), "Bad Election", fake.lorem().paragraph() + "ass", "TEST", Arrays.asList(fake.funnyName().name(), fake.funnyName().name(), "FUCK Cillary Hlinton"));
    final Map<String, Long> candidateMap = new HashMap<>();
    legalElection.getCandidates().forEach(c -> candidateMap.put(c, 0L));

    final Election expected = Election.builder()
            .author(legalElection.getAuthor())
            .title(legalElection.getTitle())
            .description(legalElection.getDescription())
            .category(legalElection.getCategory())
            .candidates(candidateMap)
            .status(ElectionStatus.OPEN)
            .build();


    inputTopic.pipeKeyValueList(Arrays.asList(
            new KeyValue<>(fake.idNumber().valid(), TestCEBuilder.buildCE(legalElection)),
            new KeyValue<>(fake.idNumber().valid(), TestCEBuilder.buildCE(illegalElection))
    ));

    assertThat(outputTopic.getQueueSize()).isOne();
    final CloudEvent actual = outputTopic.readKeyValue().value;
    assertThat(StreamUtils.unwrapCloudEventData(actual.getData(), Election.class))
            .satisfies(election -> assertThat(election.getId()).isNotNull())
            .satisfies(election -> assertThat(election.getAuthor()).isEqualTo(expected.getAuthor()))
            .satisfies(election -> assertThat(election.getTitle()).isEqualTo(expected.getTitle()))
            .satisfies(election -> assertThat(election.getDescription()).isEqualTo(expected.getDescription()))
            .satisfies(election -> assertThat(election.getCategory()).isEqualTo(expected.getCategory()))
            .satisfies(election -> assertThat(election.getCandidates()).isEqualTo(expected.getCandidates()))
            .satisfies(election -> assertThat(election.getStatus()).isEqualTo(expected.getStatus()))
            .satisfies(election -> assertThat(election.getEndTs()).isNotNull())
            .satisfies(election -> assertThat(election.getStartTs()).isNotNull());
  }



}
