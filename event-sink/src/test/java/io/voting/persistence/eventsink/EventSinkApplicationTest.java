package io.voting.persistence.eventsink;

import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.clients.serialization.avro.KafkaAvroCloudEventSerializer;
import io.voting.common.library.kafka.test.TestSender;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionStatus;
import io.voting.events.enums.ElectionCategory;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewElection;
import io.voting.events.integrity.NewVote;
import io.voting.persistence.eventsink.framework.TestKafkaContext;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EventSinkApplicationTest extends TestKafkaContext {

  static final String INPUT_TOPIC = "election.events";
  static final Faker fake = Faker.instance();

  @DynamicPropertySource
  static void registerKafkaProperties(final DynamicPropertyRegistry dynamicPropertyRegistry) {
    dynamicPropertyRegistry.add("kafka.properties.bootstrap.servers", kafkaContainer::getBootstrapServers);
    dynamicPropertyRegistry.add("kafka.properties.schema.registry.url", TestKafkaContext::schemaRegistryUrl);
  }

  static EventSender<String, CloudEvent> eventSender;

  @SneakyThrows
  @BeforeAll
  static void init() {
    final Map<String, Object> producerConfigs = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
    producerConfigs.put(CloudEventSerializer.ENCODING_CONFIG, "BINARY");
    producerConfigs.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, TestKafkaContext.schemaRegistryUrl());
    producerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroCloudEventSerializer.class);
    final KafkaProducer<String, CloudEvent> producer = new KafkaProducer<>(producerConfigs);
    eventSender = new TestSender<>(INPUT_TOPIC, producer);
    Thread.sleep(1000);
  }

  @SneakyThrows
  @AfterAll
  static void shutdown() {
    eventSender.close();
    kafkaContainer.close();

  }

  @Autowired
  private MongoTemplate template;

  @AfterEach
  void cleanup() {
    template.getDb().drop();
  }

  @ParameterizedTest
  @MethodSource
  void testElectionCreateEvents(CloudEvent electionCreate) {
    assertThat(template.findAll(Election.class)).isEmpty();
    eventSender.blockingSend(electionCreate);

    Awaitility.waitAtMost(Duration.ofSeconds(3)).untilAsserted(
            () -> assertThat(template.findAll(Election.class)).hasSize(1)
    );

  }

  static Stream<Arguments> testElectionCreateEvents() {
    return Stream.of(
      Arguments.of(buildElectionCreate(Map.of(fake.harryPotter().house().replace(".", "_"), 0L, fake.harryPotter().character().replace(".", "_"), 0L))),
      Arguments.of(buildElectionCreate(Map.of(fake.hobbit().location().replace(".", "_"), 0L, fake.hobbit().character().replace(".", "_"), 0L))),
      Arguments.of(buildElectionCreate(Map.of(fake.lordOfTheRings().location().replace(".", "_"), 0L, fake.lordOfTheRings().character().replace(".", "_"), 0L))),
      Arguments.of(buildElectionCreate(Map.of(fake.friends().location().replace(".", "_"), 0L, fake.friends().character().replace(".", "_"), 0L))),
      Arguments.of(buildElectionCreate(Map.of(fake.howIMetYourMother().highFive().replace(".", "_"), 0L, fake.howIMetYourMother().character().replace(".", "_"), 0L))),
      Arguments.of(buildElectionCreate(Map.of(fake.lebowski().actor().replace(".", "_"), 0L, fake.lebowski().character().replace(".", "_"), 0L))),
      Arguments.of(buildElectionCreate(Map.of(fake.gameOfThrones().dragon().replace(".", "_"), 0L, fake.gameOfThrones().character().replace(".", "_"), 0L))),
      Arguments.of(buildElectionCreate(Map.of(fake.pokemon().location().replace(".", "_"), 0L, fake.pokemon().name().replace(".", "_"), 0L))),
      Arguments.of(buildElectionCreate(Map.of(fake.leagueOfLegends().location().replace(".", "_"), 0L, fake.leagueOfLegends().champion().replace(".", "_"), 0L)))
    );
  }

  static CloudEvent buildElectionCreate(final Map<String, Long> candidates) {
    final Map<CharSequence, Long> cMap = new HashMap<>(candidates);
    final NewElection election = NewElection.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setAuthor(fake.funnyName().name())
            .setTitle(fake.harryPotter().spell())
            .setDescription(fake.lorem().paragraph())
            .setCategory(ElectionCategory.Random)
            .setCandidates(cMap)
            .setStartTs(Instant.now())
            .setEndTs(Instant.now())
            .setStatus(io.voting.events.enums.ElectionStatus.OPEN)
            .build();
    return CloudEventHelper.builder.withId("123").withType(NewElection.class.getName()).withData(AvroCloudEventData.MIME_TYPE, new AvroCloudEventData<>(new IntegrityEvent(election))).build();
  }


  @ParameterizedTest
  @MethodSource
  void testElectionUpdateEvents(Map<String, Long> candidates, List<String> votes, Map<String, Long> expectedResults) {
    final Election election = template.insert(
            new Election(null, fake.funnyName().name(), fake.harryPotter().spell(), fake.lorem().paragraph(), "TEST", candidates, 0L, 0L, ElectionStatus.OPEN));
    assertThat(election.getId()).isNotNull();

    for (String vote : votes) {
      final NewVote newVote = NewVote.newBuilder().setEId(election.getId()).setCandidate(vote).setUId("someUser").build();
      final CloudEvent ce = CloudEventHelper.builder.withId(election.getId()).withType(NewVote.class.getName()).withData(AvroCloudEventData.MIME_TYPE, new AvroCloudEventData<>(new IntegrityEvent(newVote))).build();
      eventSender.blockingSend(ce);
    }

    Awaitility.waitAtMost(Duration.ofSeconds(5)).untilAsserted(
            () -> assertThat(Objects.requireNonNull(template.findById(election.getId(), Election.class)).getCandidates())
                    .isEqualTo(expectedResults)
    );

  }

  static Stream<Arguments> testElectionUpdateEvents() {
    return Stream.of(
        Arguments.of(
                Map.of(CandidateTypes.FooBar.FOO, 0L, CandidateTypes.FooBar.BAR, 0L),
                Arrays.asList(
                        CandidateTypes.FooBar.FOO,
                        CandidateTypes.FooBar.FOO,
                        CandidateTypes.FooBar.FOO,
                        CandidateTypes.FooBar.FOO,
                        CandidateTypes.FooBar.FOO,
                        CandidateTypes.FooBar.BAR,
                        CandidateTypes.FooBar.BAR,
                        CandidateTypes.FooBar.BAR,
                        CandidateTypes.FooBar.BAR,
                        CandidateTypes.FooBar.BAR
                ),
                Map.of(CandidateTypes.FooBar.FOO, 5L, CandidateTypes.FooBar.BAR, 5L)
        ),
        Arguments.of(
                Map.of(CandidateTypes.Food.HAMBURGER, 0L, CandidateTypes.Food.HOTDOG, 0L, CandidateTypes.Food.PIZZA, 0L, CandidateTypes.Food.STEAK, 0L),
                Arrays.asList(
                        CandidateTypes.Food.HAMBURGER,
                        CandidateTypes.Food.HOTDOG,
                        CandidateTypes.Food.HOTDOG,
                        CandidateTypes.Food.PIZZA,
                        CandidateTypes.Food.PIZZA,
                        CandidateTypes.Food.PIZZA,
                        CandidateTypes.Food.STEAK,
                        CandidateTypes.Food.STEAK,
                        CandidateTypes.Food.STEAK,
                        CandidateTypes.Food.STEAK
                ),
                Map.of(CandidateTypes.Food.HAMBURGER, 1L, CandidateTypes.Food.HOTDOG, 2L, CandidateTypes.Food.PIZZA, 3L, CandidateTypes.Food.STEAK, 4L)
        ),
        Arguments.of(
                Map.of(CandidateTypes.FastFood.ARBYS, 0L, CandidateTypes.FastFood.BURGERKING, 0L, CandidateTypes.FastFood.CULVERS, 0L, CandidateTypes.FastFood.MCDONALDS, 0L, CandidateTypes.FastFood.SUBWAY, 0L, CandidateTypes.FastFood.WENDYS, 0L),
                Arrays.asList(
                  CandidateTypes.FastFood.ARBYS,
                  CandidateTypes.FastFood.BURGERKING,
                  CandidateTypes.FastFood.BURGERKING,
                  CandidateTypes.FastFood.CULVERS,
                  CandidateTypes.FastFood.CULVERS,
                  CandidateTypes.FastFood.CULVERS,
                  CandidateTypes.FastFood.MCDONALDS,
                  CandidateTypes.FastFood.MCDONALDS,
                  CandidateTypes.FastFood.MCDONALDS,
                  CandidateTypes.FastFood.MCDONALDS,
                  CandidateTypes.FastFood.SUBWAY,
                  CandidateTypes.FastFood.SUBWAY,
                  CandidateTypes.FastFood.SUBWAY,
                  CandidateTypes.FastFood.SUBWAY,
                  CandidateTypes.FastFood.SUBWAY,
                  CandidateTypes.FastFood.WENDYS,
                  CandidateTypes.FastFood.WENDYS,
                  CandidateTypes.FastFood.WENDYS,
                  CandidateTypes.FastFood.WENDYS,
                  CandidateTypes.FastFood.WENDYS,
                  CandidateTypes.FastFood.WENDYS
                ),
                Map.of(CandidateTypes.FastFood.ARBYS, 1L, CandidateTypes.FastFood.BURGERKING, 2L, CandidateTypes.FastFood.CULVERS, 3L, CandidateTypes.FastFood.MCDONALDS, 4L, CandidateTypes.FastFood.SUBWAY, 5L, CandidateTypes.FastFood.WENDYS, 6L)
        )
    );
  }


  static class CloudEventHelper {
    public static final CloudEventBuilder builder = new CloudEventBuilder()
            .newBuilder()
            .withSource(URI.create("http://" + EventSinkApplicationTest.class.getSimpleName()))
            .withSubject("TEST");

  }

  interface CandidateTypes {
    class FooBar {
      static final String FOO = "FOO";
      static final String BAR = "BAR";
    }
    class Food {
      static final String HAMBURGER = "HAMBURGER";
      static final String HOTDOG = "HOTDOG";
      static final String PIZZA = "PIZZA";
      static final String STEAK = "STEAK";
    }
    class FastFood {
      static final String ARBYS = "ARBYS";
      static final String BURGERKING = "BURGER KING";
      static final String CULVERS = "CULVERS";
      static final String MCDONALDS = "MCDONALDS";
      static final String SUBWAY = "SUBWAY";
      static final String WENDYS = "WENDYS";
    }
  }
}
