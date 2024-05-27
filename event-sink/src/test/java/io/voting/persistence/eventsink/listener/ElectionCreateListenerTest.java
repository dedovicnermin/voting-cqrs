package io.voting.persistence.eventsink.listener;

import com.github.javafaker.Faker;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionStatus;
import io.voting.common.library.models.ElectionVote;
import io.voting.events.enums.ElectionCategory;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewElection;
import io.voting.persistence.eventsink.dao.ElectionDao;
import io.voting.persistence.eventsink.framework.TestReceiver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionCreateListenerTest {

  static final Faker fake = Faker.instance();

  private EventListener<String, CloudEvent> listener;
  private TestDao mockDao;

  @BeforeEach
  void setup() {
    mockDao = new TestDao();
    listener = new ElectionCreateListener(new TestReceiver(), mockDao);
  }

  @Test
  void testElectionCreateEvent() {
    final NewElection election = NewElection.newBuilder()
            .setId("123")
            .setAuthor(fake.funnyName().name())
            .setTitle(fake.lordOfTheRings().location())
            .setDescription(fake.lorem().sentence())
            .setCategory(ElectionCategory.Random)
            .setCandidates(Map.of("Foo", 0L, "Bar", 0L))
            .setStartTs(Instant.now())
            .setEndTs(Instant.now())
            .setStatus(io.voting.events.enums.ElectionStatus.OPEN)
            .build();

    final CloudEvent ce = new CloudEventBuilder()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create("http://test"))
            .withSubject("TEST")
            .withType(NewElection.class.getName())
            .withData(AvroCloudEventData.MIME_TYPE, new AvroCloudEventData<>(new IntegrityEvent(election)))
            .build();
    listener.onEvent(new ReceiveEvent<>("test", 0, 0L, 0L, null, new PayloadOrError<>(ce, null, "unit test event")));
    assertThat(mockDao.electionCount()).isOne();
  }

  @Test
  void testElectionVoteEvent() {
    final ElectionVote electionVote = new ElectionVote("000", "foo");
    final CloudEvent ce = new CloudEventBuilder()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create("http://test"))
            .withSubject("TEST")
            .withType(CloudEventTypes.ELECTION_VOTE_EVENT)
            .withData(StreamUtils.wrapCloudEventData(electionVote))
            .build();
    listener.onEvent(new ReceiveEvent<>("test", 0, 1L, 0L, null, new PayloadOrError<>(ce, null, "unit test event")));
    assertThat(mockDao.electionCount()).isZero();
  }


  @Test
  void testOnErrorEvent() {
    listener.onEvent(new ReceiveEvent<>("test", 0, 2L, 0L, null, new PayloadOrError<>(null, new RuntimeException("ON PURPOSE"), "unit test error event")));
    assertThat(mockDao.electionCount()).isZero();
  }

  @Test
  void testElectionCreateEventOnException() {
    final Election election = new Election(null, fake.funnyName().name(), fake.lordOfTheRings().location(), fake.lorem().sentence(), "TEST", Map.of("Foo", 0L, "Bar", 0L), 0L, 0L, ElectionStatus.OPEN);
    final CloudEvent ce = new CloudEventBuilder()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create("http://test"))
            .withSubject("TEST")
            .withType(CloudEventTypes.ELECTION_CREATE_EVENT)
            .withData(StreamUtils.wrapCloudEventData(election))
            .build();
    mockDao.throwOnNextInvoke();

    Assertions.assertDoesNotThrow(() -> listener.onEvent(new ReceiveEvent<>("test", 0, 3L, 0L, null, new PayloadOrError<>(ce, null, "unit test event"))));
  }


  static class TestDao implements ElectionDao {
    private final Map<String, Election> elections = new HashMap<>();
    private boolean throwException = false;

    public int electionCount() {
      return elections.size();
    }
    public void throwOnNextInvoke() {
      throwException = true;
    }

    @Override
    public Election insertElection(Election election) {
      if (throwException) throw new RuntimeException("ON PURPOSE");
      election.setId(UUID.randomUUID().toString());
      elections.put(election.getId(), election);
      return election;
    }

    /** Not being tested */
    @Override
    public Election updateElection(ElectionVote electionVote) {return null;}

    /** Not being tested */
    @Override
    public Election updateElectionStatus(String electionId, ElectionStatus electionStatus) {
      return null;
    }
  }
}