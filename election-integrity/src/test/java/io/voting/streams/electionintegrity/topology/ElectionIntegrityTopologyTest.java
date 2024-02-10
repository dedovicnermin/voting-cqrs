package io.voting.streams.electionintegrity.topology;

import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
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
  static final Serde<ElectionCreate> ELECTION_CREATE_SERDE = StreamUtils.getJsonSerde(ElectionCreate.class);
  static final Serde<CloudEvent> CLOUD_EVENT_SERDE = StreamUtils.getCESerde();

  static Topology topology;
  static Properties properties;
  TopologyTestDriver testDriver;
  TestInputTopic<String, ElectionCreate> inputTopic;
  TestOutputTopic<String, CloudEvent> outputTopic;


  @BeforeAll
  static void build() {
    properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "election-integrity.test");
    properties.put("input.topic", INPUT_TOPIC);
    properties.put("output.topic", OUTPUT_TOPIC);

    final StreamsBuilder builder = new StreamsBuilder();
    topology = ElectionIntegrityTopology.buildTopology(builder, properties);
  }

  @BeforeEach
  void setup() {
    testDriver = new TopologyTestDriver(topology, properties);
    inputTopic = testDriver.createInputTopic(INPUT_TOPIC, STRING_SERDE.serializer(), ELECTION_CREATE_SERDE.serializer());
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
    final Election expected = new Election(null, legalElection.getAuthor(), legalElection.getTitle(), legalElection.getDescription(), legalElection.getCategory(), candidateMap);

    inputTopic.pipeKeyValueList(Arrays.asList(
            new KeyValue<>(fake.idNumber().valid(), legalElection),
            new KeyValue<>(fake.idNumber().valid(), illegalElection)
    ));

    assertThat(outputTopic.getQueueSize()).isOne();
    final CloudEvent actual = outputTopic.readKeyValue().value;
    assertThat(StreamUtils.unwrapCloudEventData(actual.getData(), Election.class)).isEqualTo(expected);
  }

}
