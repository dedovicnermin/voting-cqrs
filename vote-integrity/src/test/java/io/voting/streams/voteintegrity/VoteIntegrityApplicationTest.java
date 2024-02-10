package io.voting.streams.voteintegrity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.voteintegrity.config.Constants;
import io.voting.streams.voteintegrity.framework.TestConsumerHelper;
import io.voting.common.library.kafka.test.TestSender;
import io.voting.streams.voteintegrity.framework.TestKafkaContext;
import io.voting.streams.voteintegrity.topology.VoteIntegrityTopology;
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
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class VoteIntegrityApplicationTest extends TestKafkaContext {

  private static final Properties properties = new Properties();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static EventSender<String, ElectionVote> testSender;
  private static TestConsumerHelper consumerHelper;
  private static KafkaStreams app;

  final PojoCloudEventDataMapper<ElectionVote> dataMapper = PojoCloudEventDataMapper.from(objectMapper, ElectionVote.class);


  @BeforeAll
  static void init() {
    consumerHelper = new TestConsumerHelper(kafkaContainer);
    final KafkaProducer<String, ElectionVote> producer = new KafkaProducer<>(
            KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers()),
            new StringSerializer(), new JsonSerializer<>()
    );
    testSender = new TestSender<>(Constants.INPUT_TOPIC_CONFIG_DEFAULT, producer);
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "voteintegriy."+ UUID.randomUUID());
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
    properties.put(Constants.INPUT_TOPIC_CONFIG, Constants.INPUT_TOPIC_CONFIG_DEFAULT);
    properties.put(Constants.OUTPUT_TOPIC_CONFIG, Constants.OUTPUT_TOPIC_CONFIG_DEFAULT);
    properties.put(ProducerConfig.LINGER_MS_CONFIG, 0);
    properties.put("cache.max.bytes.buffering", 0);
    properties.put("commit.interval.ms", 1000);
    final Topology topology = VoteIntegrityTopology.buildTopology(new StreamsBuilder(), properties, objectMapper);
    app = new KafkaStreams(topology, properties);
    app.start();

  }

  @AfterAll
  static void shutdown() throws IOException {
    System.out.println("TEST IS CLOSING");
    app.close();
    testSender.close();
  }


  @AfterEach
  void clear() {
    consumerHelper.clearQueues();
  }


  private final Faker fake = Faker.instance();
  private final String eCandidate = fake.name().fullName();

  @Test
  void test() throws ExecutionException, InterruptedException {
    final String userId_1 = fake.name().username();
    final String userId_2 = fake.name().username();
    final String userId_3 = fake.name().username();
    final String userId_4 = fake.name().username();

    /*
     * Round-robin vote submission b/w the four users
     */
    String eId = "111";
    for (int i = 0; i < 25; i++) {
      testSender.send(eventKey(userId_1, eId), ElectionVote.of(eId, eCandidate)).get();
      testSender.send(eventKey(userId_2, eId), ElectionVote.of(eId, eCandidate)).get();
      testSender.send(eventKey(userId_3, eId), ElectionVote.of(eId, eCandidate)).get();
      testSender.send(eventKey(userId_4, eId), ElectionVote.of(eId, eCandidate)).get();
      Thread.sleep(100);
    }

    final List<ReceiveEvent<String, CloudEvent>> actualEvents = new ArrayList<>();
    consumerHelper.getEvents().drainTo(actualEvents);

    assertThat(actualEvents).hasSize(4);

    final Set<String> actualEventKeys = actualEvents.stream()
            .map(ReceiveEvent::getKey)
            .collect(Collectors.toSet());
    assertThat(actualEventKeys).hasSize(1).containsOnly(eId);

    final Set<String> actualVotedFor = actualEvents.stream()
            .map(ReceiveEvent::getPOrE)
            .map(PayloadOrError::getPayload)
            .map(CloudEvent::getData)
            .map(data -> dataMapper.map(data).getValue())
            .map(ElectionVote::getVotedFor)
            .collect(Collectors.toSet());
    assertThat(actualVotedFor).hasSize(1).containsOnly(eCandidate);

  }


  private String eventKey(final String userId, final String electionId) {
    return userId + ":" + electionId;
  }

}