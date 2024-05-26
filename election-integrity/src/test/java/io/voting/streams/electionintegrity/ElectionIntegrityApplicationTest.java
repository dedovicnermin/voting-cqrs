package io.voting.streams.electionintegrity;

import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.clients.serialization.avro.KafkaAvroCloudEventSerializer;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.test.TestSender;
import io.voting.events.cmd.CreateElection;
import io.voting.events.enums.ElectionCategory;
import io.voting.events.enums.ElectionStatus;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewElection;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Instant;
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
    properties.put("output.topic.elections", TestConsumerHelper.OUTPUT_TOPIC_ELECTION);
    properties.put("output.topic.votes", "election.votes");
    properties.put("commit.interval.ms", 1000);
    properties.put("election.ttl", "PT5M");
    properties.put(CloudEventSerializer.ENCODING_CONFIG, "BINARY");
    properties.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, TestKafkaContext.schemaRegistryUrl());
    properties.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "true");
    properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");

    consumerHelper = new TestConsumerHelper(kafkaContainer, TestConsumerHelper.OUTPUT_TOPIC_ELECTION);
    final Map<String, Object> producerConfigs = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
    producerConfigs.put(CloudEventSerializer.ENCODING_CONFIG, "BINARY");
    producerConfigs.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, TestKafkaContext.schemaRegistryUrl());
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

  @ParameterizedTest
  @MethodSource
  void testElection(CreateElection legalElection, CreateElection illegalElection) throws ExecutionException, InterruptedException {
    testSender.send(UUID.randomUUID().toString(), TestCEBuilder.buildCE(legalElection)).get();
    testSender.send(UUID.randomUUID().toString(), TestCEBuilder.buildCE(illegalElection)).get();

    final NewElection expected = NewElection.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setAuthor(legalElection.getAuthor())
            .setTitle(legalElection.getTitle())
            .setDescription(legalElection.getDescription())
            .setCategory(legalElection.getCategory())
            .setCandidates(buildCandidateMap(legalElection.getCandidates()))
            .setStatus(ElectionStatus.OPEN)
            .setStartTs(Instant.now())
            .setEndTs(Instant.now())
            .build();

    final ReceiveEvent<String, CloudEvent> actual = consumerHelper.getEvents().poll(5000, TimeUnit.MILLISECONDS);
    assertThat(consumerHelper.getEvents().poll(100, TimeUnit.MILLISECONDS)).isNull();
    assertThat(actual).isNotNull();

    final CloudEvent actualPayload = actual.getPOrE().getPayload();
    assertThat((NewElection)AvroCloudEventData.<IntegrityEvent>dataOf(actualPayload.getData()).getLegalEvent())
            .satisfies(election -> assertThat(election.getId()).isNotNull())
            .satisfies(election -> assertThat(election.getAuthor()).isEqualTo(expected.getAuthor()))
            .satisfies(election -> assertThat(election.getTitle()).isEqualTo(expected.getTitle()))
            .satisfies(election -> assertThat(election.getDescription()).isEqualTo(expected.getDescription()))
            .satisfies(election -> assertThat(election.getCategory()).isEqualTo(expected.getCategory()))
            .satisfies(election -> assertThat(formatCandidateMap(election.getCandidates())).isEqualTo(formatCandidateMap(expected.getCandidates())))
            .satisfies(election -> assertThat(election.getStatus()).isEqualTo(expected.getStatus()))
            .satisfies(election -> assertThat(election.getEndTs()).isNotNull())
            .satisfies(election -> assertThat(election.getStartTs()).isNotNull());

  }

  static Stream<Arguments> testElection() {
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

  private static CreateElection electionCreate(final String candidate) {
    return CreateElection.newBuilder()
            .setAuthor(fake.hobbit().character())
            .setTitle(fake.hobbit().location())
            .setDescription(fake.lorem().paragraph())
            .setCategory(ElectionCategory.Random)
            .setCandidates(Arrays.asList(fake.harryPotter().character(), fake.harryPotter().character(), Optional.ofNullable(candidate).orElse(fake.harryPotter().character())))
            .build();
  }

  private static Map<CharSequence, Long> buildCandidateMap(final List<CharSequence> candidates) {
    final Map<CharSequence, Long> cMap = new HashMap<>();
    candidates.forEach(c -> cMap.put(c, 0L));
    return cMap;
  }

  private static Map<String, Long> formatCandidateMap(final Map<CharSequence, Long> candidates) {
    final Map<String, Long> cMap = new HashMap<>();
    candidates.forEach((k, v) -> cMap.put(k.toString(), v));
    return cMap;

  }

}