package io.voting.streams.electionintegrity.topology;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.events.cmd.CreateElection;
import io.voting.events.cmd.RegisterVote;
import io.voting.events.cmd.ViewElection;
import io.voting.events.enums.ElectionCategory;
import io.voting.events.enums.ElectionView;
import io.voting.events.integrity.CloseElection;
import io.voting.events.integrity.IntegrityEvent;
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
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

class TTLTopologyTest {
  static final String ELECTION_COMMANDS = "election.commands";
  static final String ELECTION_REQUESTS = "election.requests";
  static final String ELECTION_VOTES = "election.votes";

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
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "election-ttl.test");
    properties.put("election.ttl", "PT15M");
    properties.put("input.topic", ELECTION_COMMANDS);
    properties.put("output.topic.elections", ELECTION_REQUESTS);
    properties.put("output.topic.votes", ELECTION_VOTES);
    properties.put(CloudEventSerializer.ENCODING_CONFIG, "BINARY");
    properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://mock");
    properties.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "true");
    properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
    MockSchemaRegistryClient srClient = new MockSchemaRegistryClient();
    CLOUD_EVENT_SERDE = StreamUtils.getAvroCESerde(srClient, properties);

    final StreamsBuilder builder = new StreamsBuilder();
    topology = ElectionIntegrityTopology.buildTopology(builder, properties, srClient);
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
    final CreateElection election = CreateElection.newBuilder()
            .setAuthor("testAuthor")
            .setTitle("testTitle")
            .setDescription("test desc")
            .setCategory(ElectionCategory.Random)
            .setCandidates(Arrays.asList("Foo", "Bar"))
            .build();

    inputTopic.pipeInput("userId0", TestCEBuilder.buildCE(election));
    final KeyValue<String, Object> legalElection = outputTopic.readKeyValue();
    assertThat(legalElection).isNotNull();

    inputTopic.pipeInput(legalElection.key, TestCEBuilder.buildCE(legalElection.key, ViewElection.newBuilder().setView(io.voting.events.enums.ElectionView.OPEN).setEId(legalElection.key).build()));
    final CloudEvent voteEvent = TestCEBuilder.buildCE(RegisterVote.newBuilder().setEId(legalElection.key).setVotedFor("Foo").build());
    inputTopic.pipeInput("userId0:" + legalElection.key, voteEvent);
    inputTopic.pipeInput("userId1:" + legalElection.key, voteEvent);
    inputTopic.pipeInput("userId2:" + legalElection.key, voteEvent);
    assertThat(outputTopic.getQueueSize()).isZero();

    inputTopic.pipeInput(legalElection.key, TestCEBuilder.buildCE(legalElection.key, ViewElection.newBuilder().setEId(legalElection.key).setView(io.voting.events.enums.ElectionView.PENDING).build()));
    assertThat(outputTopic.getQueueSize()).isOne();
    final KeyValue<String, Object> actualTTLEvent = outputTopic.readKeyValue();
    assertThat(actualTTLEvent.key).isEqualTo(legalElection.key);

    final CloudEvent actualRecordValue = (CloudEvent)actualTTLEvent.value;
    assertThat(actualRecordValue.getType()).isEqualTo(CloseElection.class.getName());
    assertThat(actualRecordValue.getData()).isInstanceOf(AvroCloudEventData.class);

    final CloseElection actualCEData = (CloseElection)AvroCloudEventData.<IntegrityEvent>dataOf(actualRecordValue.getData()).getLegalEvent();

    assertThat(actualCEData.getEId()).hasToString(legalElection.key);

    // only PENDING
    inputTopic.pipeInput(legalElection.key, TestCEBuilder.buildCE(legalElection.key, ViewElection.newBuilder().setEId(legalElection.key).setView(ElectionView.CLOSED).build()));
    assertThat(outputTopic.getQueueSize()).isZero();

  }

}
