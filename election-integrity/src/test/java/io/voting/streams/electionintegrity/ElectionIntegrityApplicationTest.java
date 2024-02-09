package io.voting.streams.electionintegrity;

import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.test.TestSender;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.streams.electionintegrity.framework.TestConsumerHelper;
import io.voting.streams.electionintegrity.framework.TestKafkaContext;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionIntegrityApplicationTest extends TestKafkaContext {

  static final Properties properties = new Properties();
  static final Faker fake = Faker.instance();
  static EventSender<String, ElectionCreate> testSender;
  static TestConsumerHelper consumerHelper;
  static KafkaStreams app;

  @BeforeAll
  static void init() {
    consumerHelper = new TestConsumerHelper(kafkaContainer);
    final KafkaProducer<String, ElectionCreate> producer = new KafkaProducer<>(
            KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers()),
            new StringSerializer(), new JsonSerializer<>()
    );
    testSender = new TestSender<>(TestConsumerHelper.INPUT_TOPIC, producer);
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "election.integrity."+ UUID.randomUUID());
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
    properties.put(ProducerConfig.LINGER_MS_CONFIG, 0);
    properties.put("cache.max.bytes.buffering", 0);
    properties.put("input.topic", TestConsumerHelper.INPUT_TOPIC);
    properties.put("output.topic", TestConsumerHelper.OUTPUT_TOPIC);
    properties.put("commit.interval.ms", 1000);
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

  @ParameterizedTest
  @MethodSource
  void test(ElectionCreate legalElection, ElectionCreate illegalElection) throws ExecutionException, InterruptedException {
    testSender.send(UUID.randomUUID().toString(), legalElection).get();
    testSender.send(UUID.randomUUID().toString(), illegalElection).get();

    final Election expected = new Election(null, legalElection.getAuthor(), legalElection.getTitle(), legalElection.getDescription(), legalElection.getCategory(), buildCandidateMap(legalElection.getCandidates()));
    final ReceiveEvent<String, CloudEvent> actual = consumerHelper.getEvents().poll(1000, TimeUnit.MILLISECONDS);
    assertThat(consumerHelper.getEvents().poll(100, TimeUnit.MILLISECONDS)).isNull();
    assertThat(actual).isNotNull();

    final CloudEvent actualPayload = actual.getPOrE().getPayload();
    assertThat(StreamUtils.unwrapCloudEventData(actualPayload.getData(), Election.class)).isEqualTo(expected);

  }

  static Stream<Arguments> test() {
    return Stream.of(
            Arguments.of(electionCreate("suck"), electionCreate("fuck")),
            Arguments.of(electionCreate(null), electionCreate("shit")),
            Arguments.of(electionCreate("osshole"), electionCreate("asshole")),
            Arguments.of(electionCreate("ditch"), electionCreate("bitch")),
            Arguments.of(electionCreate("nussy"), electionCreate("pussy")),
            Arguments.of(electionCreate(null), electionCreate("cunt")),
            Arguments.of(electionCreate(null), electionCreate("prick")),
            Arguments.of(electionCreate("corn"), electionCreate("porn"))
    );
  }


  private static ElectionCreate electionCreate(final String candidate) {
    return new ElectionCreate(
            fake.hobbit().character(),
            fake.hobbit().location(),
            fake.lorem().paragraph(),
            "TEST",
            Arrays.asList(fake.harryPotter().character(), fake.harryPotter().character(), Optional.ofNullable(candidate).orElse(fake.harryPotter().character()))
    );
  }

  private static Map<String, Long> buildCandidateMap(final List<String> candidates) {
    final Map<String, Long> cMap = new HashMap<>();
    candidates.forEach(c -> cMap.put(c, 0L));
    return cMap;
  }

}