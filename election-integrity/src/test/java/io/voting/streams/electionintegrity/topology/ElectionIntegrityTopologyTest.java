package io.voting.streams.electionintegrity.topology;

import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.events.cmd.CreateElection;
import io.voting.events.enums.ElectionCategory;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewElection;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionIntegrityTopologyTest {

  static final String INPUT_TOPIC = "election.requests.raw";
  static final String OUTPUT_TOPIC = "election.requests";

  static final Serde<String> STRING_SERDE = Serdes.String();
  static Serde<Object> CLOUD_EVENT_SERDE;

  static Topology topology;
  static Properties properties;
  TopologyTestDriver testDriver;
  TestInputTopic<String, Object> inputTopic;
  TestOutputTopic<String, Object> outputTopic;


  @BeforeAll
  static void build() {
    properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "election-integrity.test");
    properties.put("input.topic", INPUT_TOPIC);
    properties.put("output.topic", OUTPUT_TOPIC);
    properties.put("election.ttl", "P2D");
    properties.put(CloudEventSerializer.ENCODING_CONFIG, "BINARY");
    properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://mock");
    properties.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "true");
    properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
    MockSchemaRegistryClient srClient = new MockSchemaRegistryClient();
    CLOUD_EVENT_SERDE = StreamUtils.getAvroCESerde(srClient, properties);

    final StreamsBuilder builder = new StreamsBuilder();
    topology = ElectionIntegrityTopology.buildTopology(builder, properties, srClient);
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

    final CreateElection legalElection = CreateElection.newBuilder()
            .setAuthor(fake.funnyName().name())
            .setTitle("Good Election")
            .setDescription(fake.lorem().paragraph())
            .setCategory(ElectionCategory.Random)
            .setCandidates(Arrays.asList(fake.funnyName().name(), fake.funnyName().name()))
            .build();

    final CreateElection illegalElection = CreateElection.newBuilder()
            .setAuthor(fake.funnyName().name())
            .setTitle("Bad Election")
            .setDescription(fake.lorem().paragraph() + "ass")
            .setCategory(ElectionCategory.Random)
            .setCandidates(Arrays.asList(fake.funnyName().name(), fake.funnyName().name(), "Asshole Cillary Hlinton"))
            .build();

    final Map<CharSequence, Long> candidateMap = new HashMap<>();
    legalElection.getCandidates().forEach(c -> candidateMap.put(c, 0L));

    final NewElection expected = NewElection.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setAuthor(legalElection.getAuthor())
            .setTitle(legalElection.getTitle())
            .setDescription(legalElection.getDescription())
            .setCategory(legalElection.getCategory())
            .setCandidates(candidateMap)
            .setStatus(io.voting.events.enums.ElectionStatus.OPEN)
            .setStartTs(Instant.now())
            .setEndTs(Instant.now())
            .build();


    inputTopic.pipeKeyValueList(Arrays.asList(
            new KeyValue<>(fake.idNumber().valid(), TestCEBuilder.buildCE(legalElection)),
            new KeyValue<>(fake.idNumber().valid(), TestCEBuilder.buildCE(illegalElection))
    ));

    assertThat(outputTopic.getQueueSize()).isOne();
    final CloudEvent actual = (CloudEvent) outputTopic.readKeyValue().value;
    assertThat((NewElection) AvroCloudEventData.<IntegrityEvent>dataOf(actual.getData()).getLegalEvent())
            .satisfies(election -> assertThat(election.getId()).isNotNull())
            .satisfies(election -> assertThat(election.getAuthor()).isEqualTo(expected.getAuthor()))
            .satisfies(election -> assertThat(election.getTitle()).isEqualTo(expected.getTitle()))
            .satisfies(election -> assertThat(election.getDescription()).isEqualTo(expected.getDescription()))
            .satisfies(election -> assertThat(election.getCategory()).isEqualTo(expected.getCategory()))
            .satisfies(election -> assertThat(election.getStatus()).isEqualTo(expected.getStatus()))
            .satisfies(election -> assertThat(election.getEndTs()).isNotNull())
            .satisfies(election -> assertThat(election.getStartTs()).isNotNull())
            .satisfies(election -> assertThat(getCandidateMap(election)).isEqualTo(getCandidateMap(expected)));
  }

  private Map<String, Long> getCandidateMap(NewElection exp) {
    Map<CharSequence, Long> candidates = exp.getCandidates();
    Map<String, Long> map = new HashMap<>();
    candidates.keySet().forEach(cs -> map.put(cs.toString(), candidates.get(cs)));
    return map;
  }



}
