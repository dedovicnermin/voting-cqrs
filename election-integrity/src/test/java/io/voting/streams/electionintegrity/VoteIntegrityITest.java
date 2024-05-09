package io.voting.streams.electionintegrity;

import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.test.TestSender;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.electionintegrity.framework.TestCmdBuilder;
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
    consumerHelper = new TestConsumerHelper(kafkaContainer, TestConsumerHelper.OUTPUT_TOPIC_VOTES);
    final KafkaProducer<String, CloudEvent> producer = new KafkaProducer<>(
            KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers()),
            new StringSerializer(), StreamUtils.getCESerde().serializer()
    );
    testSender = new TestSender<>(TestConsumerHelper.INPUT_TOPIC, producer);
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "election.integrity."+ UUID.randomUUID());
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
    properties.put(ProducerConfig.LINGER_MS_CONFIG, 0);
    properties.put("cache.max.bytes.buffering", 0);
    properties.put("input.topic", TestConsumerHelper.INPUT_TOPIC);
    properties.put("output.topic.elections", TestConsumerHelper.OUTPUT_TOPIC_ELECTION);
    properties.put("output.topic.votes", "election.votes");
    properties.put("commit.interval.ms", 1000);
    properties.put("election.ttl", "PT5M");
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
      testSender.send(eventKey(userId_1, eId), TestCmdBuilder.buildCE(ElectionVote.of(eId, eCandidate))).get();
      testSender.send(eventKey(userId_2, eId), TestCmdBuilder.buildCE(ElectionVote.of(eId, eCandidate))).get();
      testSender.send(eventKey(userId_3, eId), TestCmdBuilder.buildCE(ElectionVote.of(eId, eCandidate))).get();
      testSender.send(eventKey(userId_4, eId), TestCmdBuilder.buildCE(ElectionVote.of(eId, eCandidate))).get();
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
            .map(data -> StreamUtils.unwrapCloudEventData(data, ElectionVote.class))
            .map(ElectionVote::getVotedFor)
            .collect(Collectors.toSet());
    assertThat(actualVotedFor).hasSize(1).containsOnly(eCandidate);

  }


  private String eventKey(final String userId, final String electionId) {
    return userId + ":" + electionId;
  }

}
