package io.voting.streams.electionintegrity;

import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.clients.serialization.avro.KafkaAvroCloudEventSerializer;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.test.TestSender;
import io.voting.events.cmd.RegisterVote;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewVote;
import io.voting.streams.electionintegrity.framework.TestCEBuilder;
import io.voting.streams.electionintegrity.framework.TestConsumerHelper;
import io.voting.streams.electionintegrity.framework.TestKafkaContext;
import io.voting.streams.electionintegrity.topology.ElectionIntegrityTopology;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class VoteIntegrityITest extends TestKafkaContext {
  static final Properties properties = new Properties();
  static EventSender<String, CloudEvent> testSender;
  static TestConsumerHelper consumerHelper;
  static KafkaStreams app;

  @BeforeAll
  static void init() {
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "election.integrity."+ UUID.randomUUID());
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
    properties.put(ProducerConfig.LINGER_MS_CONFIG, 0);
    properties.put("cache.max.bytes.buffering", 0);
    properties.put("input.topic", TestConsumerHelper.INPUT_TOPIC);
    properties.put("output.topic", TestConsumerHelper.OUTPUT_TOPIC);
    properties.put("commit.interval.ms", 1000);
    properties.put("election.ttl", "PT5M");
    properties.put(CloudEventSerializer.ENCODING_CONFIG, "BINARY");
    properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, TestKafkaContext.schemaRegistryUrl());
    properties.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "true");
    properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");

    consumerHelper = new TestConsumerHelper(kafkaContainer, TestConsumerHelper.OUTPUT_TOPIC);
    final Map<String, Object> producerConfigs = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
    producerConfigs.put(CloudEventSerializer.ENCODING_CONFIG, "BINARY");
    producerConfigs.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, TestKafkaContext.schemaRegistryUrl());
    producerConfigs.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, true);
    producerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroCloudEventSerializer.class);
    final KafkaProducer<String, CloudEvent> producer = new KafkaProducer<>(producerConfigs);
    testSender = new TestSender<>(TestConsumerHelper.INPUT_TOPIC, producer);

    final Topology topology = ElectionIntegrityTopology.buildTopology(new StreamsBuilder(), properties);
    app = new KafkaStreams(topology, properties);
    app.start();
  }

  @SneakyThrows
  @AfterAll
  static void shutdown() {
    app.close();
    testSender.close();
  }

  @AfterEach
  void cleanup() {
    consumerHelper.clearQueues();
  }

  @Test
  void testVotes() throws ExecutionException, InterruptedException {
    final Faker fake = Faker.instance();
    final String eCandidate = fake.name().fullName();
    final String userId_1 = fake.name().username();
    final String userId_2 = fake.name().username();
    final String userId_3 = fake.name().username();
    final String userId_4 = fake.name().username();

    /*
     * Round-robin vote submission b/w the four users
     */
    String eId = "111";
    for (int i = 0; i < 25; i++) {

      testSender.send(eventKey(userId_1, eId), TestCEBuilder.buildCE(RegisterVote.newBuilder().setEId(eId).setVotedFor(eCandidate).build())).get();
      testSender.send(eventKey(userId_2, eId), TestCEBuilder.buildCE(RegisterVote.newBuilder().setEId(eId).setVotedFor(eCandidate).build())).get();
      testSender.send(eventKey(userId_3, eId), TestCEBuilder.buildCE(RegisterVote.newBuilder().setEId(eId).setVotedFor(eCandidate).build())).get();
      testSender.send(eventKey(userId_4, eId), TestCEBuilder.buildCE(RegisterVote.newBuilder().setEId(eId).setVotedFor(eCandidate).build())).get();
      Thread.sleep(100);
    }

    final List<ReceiveEvent<String, CloudEvent>> actualEvents = new ArrayList<>();
    consumerHelper.getEvents().drainTo(actualEvents);
    List<ReceiveEvent<String, CloudEvent>> actualTargetEvents = actualEvents.stream()
            .filter(re -> NewVote.class.getName().equals(re.getPOrE().getPayload().getType()))
            .toList();

    assertThat(actualTargetEvents).hasSize(4);

    final Set<String> actualEventKeys = actualTargetEvents.stream()
            .map(ReceiveEvent::getKey)
            .collect(Collectors.toSet());
    assertThat(actualEventKeys).hasSize(1).containsOnly(eId);

    final Set<String> actualVotedFor = actualTargetEvents.stream()
            .map(ReceiveEvent::getPOrE)
            .map(PayloadOrError::getPayload)
            .map(CloudEvent::getData)
            .map(AvroCloudEventData::<IntegrityEvent>dataOf)
            .map(integrityEvent -> (NewVote) integrityEvent.getLegalEvent())
            .map(nv -> nv.getCandidate().toString())
            .collect(Collectors.toSet());
    assertThat(actualVotedFor).hasSize(1).containsOnly(eCandidate);

  }


  private String eventKey(final String userId, final String electionId) {
    return userId + ":" + electionId;
  }

}
