package io.voting.streams.electionintegrity.topology;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.events.cmd.RegisterVote;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewVote;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VoteIntegrityTopologyTest {

  static Serde<Object> CLOUD_EVENT_SERDE;
  static Topology topology;
  TopologyTestDriver testDriver;
  TestInputTopic<String, Object> inputTopic;
  TestOutputTopic<String, Object> outputTopic;
  static Properties properties;

  @BeforeAll
  static void buildTopology() {
    properties = new Properties();
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "voting.test-"+ UUID.randomUUID());
    properties.put("input.topic", "input");
    properties.put("output.topic.elections", "output_elections");
    properties.put("output.topic.votes", "output_votes");
    properties.put("election.ttl", "P2D");
    properties.put(CloudEventSerializer.ENCODING_CONFIG, "BINARY");
    properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://mock");
    properties.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "true");
    properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
    MockSchemaRegistryClient srClient = new MockSchemaRegistryClient();
    CLOUD_EVENT_SERDE = StreamUtils.getAvroCESerde(srClient, properties);

    final StreamsBuilder streamsBuilder = new StreamsBuilder();
    topology = ElectionIntegrityTopology.buildTopology(streamsBuilder, properties, srClient);
  }

  @BeforeEach
  void setup() {
    testDriver = new TopologyTestDriver(topology, properties);
    final Serde<String> stringSerde = Serdes.String();
    inputTopic = testDriver.createInputTopic(properties.getProperty("input.topic"), stringSerde.serializer(), CLOUD_EVENT_SERDE.serializer());
    outputTopic = testDriver.createOutputTopic(properties.getProperty("output.topic.votes"), stringSerde.deserializer(), CLOUD_EVENT_SERDE.deserializer());
  }


  @AfterEach
  void cleanup() {
    testDriver.close();
  }

  @Test
  void test() {
    final String USER = "nerm";
    final String ELECTION = "001";
    final RegisterVote v1 = electionVote("Bob");
    final RegisterVote v2 = electionVote("Jack");
    final RegisterVote v3 = electionVote("Nancy");
    final RegisterVote v4 = electionVote("Jack");

    final String EVENT_KEY = USER + ":" + ELECTION;
    inputTopic.pipeKeyValueList(
            Arrays.asList(
                    new KeyValue<>(EVENT_KEY, TestCEBuilder.buildCE(v1)),
                    new KeyValue<>(EVENT_KEY, TestCEBuilder.buildCE(v1)),
                    new KeyValue<>(EVENT_KEY, TestCEBuilder.buildCE(v1)),
                    new KeyValue<>(EVENT_KEY, TestCEBuilder.buildCE(v2)),
                    new KeyValue<>(EVENT_KEY, TestCEBuilder.buildCE(v3)),
                    new KeyValue<>(EVENT_KEY, TestCEBuilder.buildCE(v4))
            )
    );
    assertThat(outputTopic.getQueueSize()).isOne();
    final KeyValue<String, Object> outputEvent = outputTopic.readKeyValue();
    assertThat(outputEvent.key).isEqualTo(ELECTION);
    final CloudEvent actualValue = (CloudEvent) outputEvent.value;
    assertThat(actualValue.getSubject()).isEqualTo(ELECTION);
    assertThat(actualValue.getData()).isNotNull();
    NewVote actualValueData = (NewVote) AvroCloudEventData.<IntegrityEvent>dataOf(actualValue.getData()).getLegalEvent();

    System.out.println();
    System.out.println(outputEvent);
    System.out.println(actualValueData);
    System.out.println();

    assertThat(actualValueData).isEqualTo(NewVote.newBuilder().setEId(ELECTION).setCandidate("Bob").setUId(USER).build());

  }

  private RegisterVote electionVote(String votedFor) {
    return RegisterVote.newBuilder()
            .setEId("001")
            .setVotedFor(votedFor)
            .build();
  }

}