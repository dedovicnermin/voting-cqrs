package io.voting.persistence.eventsink;

import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.kafka.clients.serialization.ce.CESerializer;
import io.voting.common.library.kafka.test.TestSender;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionVote;
import io.voting.persistence.eventsink.framework.TestKafkaContext;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EventSinkApplicationTest extends TestKafkaContext {

  static final String VOTE_TOPIC = "election.votes";
  static final String ELECTION_TOPIC = "election.requests";
  static final Faker fake = Faker.instance();

  @DynamicPropertySource
  static void registerKafkaProperties(final DynamicPropertyRegistry dynamicPropertyRegistry) {
    dynamicPropertyRegistry.add("kafka.properties.bootstrap.servers", kafkaContainer::getBootstrapServers);
  }

  static EventSender<String, CloudEvent> electionSender;
  static EventSender<String, CloudEvent> voteSender;

  @SneakyThrows
  @BeforeAll
  static void init() {
    final KafkaProducer<String, CloudEvent> producer = new KafkaProducer<>(
            KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers()),
            new StringSerializer(),
            new CESerializer()
    );
    electionSender = new TestSender<>(ELECTION_TOPIC, producer);
    voteSender = new TestSender<>(VOTE_TOPIC, producer);
    Thread.sleep(1000);
  }

  @SneakyThrows
  @AfterAll
  static void shutdown() {
    electionSender.close();
    voteSender.close();
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
    electionSender.blockingSend(electionCreate);

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
    final Election election = new Election(null, fake.funnyName().name(), fake.harryPotter().spell(), fake.lorem().paragraph(), "TEST", candidates);
    return CloudEventHelper.buildCloudEvent(CloudEventTypes.ELECTION_CREATE_EVENT, StreamUtils.wrapCloudEventData(election));
  }


  @ParameterizedTest
  @MethodSource
  void testElectionUpdateEvents(Map<String, Long> candidates, List<ElectionVote> votes, Map<String, Long> expectedResults) {
    final Election election = template.insert(
            new Election(null, fake.funnyName().name(), fake.harryPotter().spell(), fake.lorem().paragraph(), "TEST", candidates));
    assertThat(election.getId()).isNotNull();

    for (ElectionVote vote : votes) {
      vote.setElectionId(election.getId());
      voteSender.blockingSend(CloudEventHelper.buildCloudEvent(CloudEventTypes.ELECTION_VOTE_EVENT, StreamUtils.wrapCloudEventData(vote)));
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
                        new ElectionVote(null, CandidateTypes.FooBar.FOO),
                        new ElectionVote(null, CandidateTypes.FooBar.FOO),
                        new ElectionVote(null, CandidateTypes.FooBar.FOO),
                        new ElectionVote(null, CandidateTypes.FooBar.FOO),
                        new ElectionVote(null, CandidateTypes.FooBar.FOO),
                        new ElectionVote(null, CandidateTypes.FooBar.BAR),
                        new ElectionVote(null, CandidateTypes.FooBar.BAR),
                        new ElectionVote(null, CandidateTypes.FooBar.BAR),
                        new ElectionVote(null, CandidateTypes.FooBar.BAR),
                        new ElectionVote(null, CandidateTypes.FooBar.BAR)
                ),
                Map.of(CandidateTypes.FooBar.FOO, 5L, CandidateTypes.FooBar.BAR, 5L)
        ),
        Arguments.of(
                Map.of(CandidateTypes.Food.HAMBURGER, 0L, CandidateTypes.Food.HOTDOG, 0L, CandidateTypes.Food.PIZZA, 0L, CandidateTypes.Food.STEAK, 0L),
                Arrays.asList(
                        new ElectionVote(null, CandidateTypes.Food.HAMBURGER),
                        new ElectionVote(null, CandidateTypes.Food.HOTDOG),
                        new ElectionVote(null, CandidateTypes.Food.HOTDOG),
                        new ElectionVote(null, CandidateTypes.Food.PIZZA),
                        new ElectionVote(null, CandidateTypes.Food.PIZZA),
                        new ElectionVote(null, CandidateTypes.Food.PIZZA),
                        new ElectionVote(null, CandidateTypes.Food.STEAK),
                        new ElectionVote(null, CandidateTypes.Food.STEAK),
                        new ElectionVote(null, CandidateTypes.Food.STEAK),
                        new ElectionVote(null, CandidateTypes.Food.STEAK)
                ),
                Map.of(CandidateTypes.Food.HAMBURGER, 1L, CandidateTypes.Food.HOTDOG, 2L, CandidateTypes.Food.PIZZA, 3L, CandidateTypes.Food.STEAK, 4L)
        ),
        Arguments.of(
                Map.of(CandidateTypes.FastFood.ARBYS, 0L, CandidateTypes.FastFood.BURGERKING, 0L, CandidateTypes.FastFood.CULVERS, 0L, CandidateTypes.FastFood.MCDONALDS, 0L, CandidateTypes.FastFood.SUBWAY, 0L, CandidateTypes.FastFood.WENDYS, 0L),
                Arrays.asList(
                  new ElectionVote(null, CandidateTypes.FastFood.ARBYS),
                  new ElectionVote(null, CandidateTypes.FastFood.BURGERKING),
                  new ElectionVote(null, CandidateTypes.FastFood.BURGERKING),
                  new ElectionVote(null, CandidateTypes.FastFood.CULVERS),
                  new ElectionVote(null, CandidateTypes.FastFood.CULVERS),
                  new ElectionVote(null, CandidateTypes.FastFood.CULVERS),
                  new ElectionVote(null, CandidateTypes.FastFood.MCDONALDS),
                  new ElectionVote(null, CandidateTypes.FastFood.MCDONALDS),
                  new ElectionVote(null, CandidateTypes.FastFood.MCDONALDS),
                  new ElectionVote(null, CandidateTypes.FastFood.MCDONALDS),
                  new ElectionVote(null, CandidateTypes.FastFood.SUBWAY),
                  new ElectionVote(null, CandidateTypes.FastFood.SUBWAY),
                  new ElectionVote(null, CandidateTypes.FastFood.SUBWAY),
                  new ElectionVote(null, CandidateTypes.FastFood.SUBWAY),
                  new ElectionVote(null, CandidateTypes.FastFood.SUBWAY),
                  new ElectionVote(null, CandidateTypes.FastFood.WENDYS),
                  new ElectionVote(null, CandidateTypes.FastFood.WENDYS),
                  new ElectionVote(null, CandidateTypes.FastFood.WENDYS),
                  new ElectionVote(null, CandidateTypes.FastFood.WENDYS),
                  new ElectionVote(null, CandidateTypes.FastFood.WENDYS),
                  new ElectionVote(null, CandidateTypes.FastFood.WENDYS)
                ),
                Map.of(CandidateTypes.FastFood.ARBYS, 1L, CandidateTypes.FastFood.BURGERKING, 2L, CandidateTypes.FastFood.CULVERS, 3L, CandidateTypes.FastFood.MCDONALDS, 4L, CandidateTypes.FastFood.SUBWAY, 5L, CandidateTypes.FastFood.WENDYS, 6L)
        )
    );
  }


  static class CloudEventHelper {
    private static final CloudEventBuilder builder = new CloudEventBuilder()
            .newBuilder()
            .withSource(URI.create("http://" + EventSinkApplicationTest.class.getSimpleName()))
            .withSubject("TEST");

    public static CloudEvent buildCloudEvent(final String type, final CloudEventData data) {
      return builder
              .withId(UUID.randomUUID().toString())
              .withType(type)
              .withData(data)
              .build();
    }
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
