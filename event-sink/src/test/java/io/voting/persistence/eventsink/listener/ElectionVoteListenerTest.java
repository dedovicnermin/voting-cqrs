package io.voting.persistence.eventsink.listener;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionVote;
import io.voting.persistence.eventsink.dao.ElectionDao;
import io.voting.persistence.eventsink.framework.TestReceiver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionVoteListenerTest {

  EventListener<String, CloudEvent> listener;
  TestDao mockDao;

  @BeforeEach
  void setup() {
    mockDao = new TestDao();
    listener = new ElectionVoteListener(new TestReceiver(), mockDao);
  }

  @Test
  void testOnElectionVoteEvent() {
    final ElectionVote electionVote = new ElectionVote(UUID.randomUUID().toString(), "foo");
    final CloudEvent ce = new CloudEventBuilder()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create("http://test"))
            .withSubject("TEST")
            .withType(CloudEventTypes.ELECTION_VOTE_EVENT)
            .withData(StreamUtils.wrapCloudEventData(electionVote))
            .build();

    listener.onEvent(new ReceiveEvent<>("test", 0, 0L, 0L, null, new PayloadOrError<>(ce, null, "unit test event")));

    assertThat(mockDao.getCount()).isOne();
  }

  @Test
  void testOnElectionCreateEvent() {
    final CloudEvent ce = new CloudEventBuilder()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create("http://test"))
            .withSubject("TEST")
            .withType(CloudEventTypes.ELECTION_CREATE_EVENT)
            .build();

    listener.onEvent(new ReceiveEvent<>("test", 0, 1L, 0L, null, new PayloadOrError<>(ce, null, "unit test event")));

    assertThat(mockDao.getCount()).isZero();
  }

  @Test
  void testOnErrorEvent() {
    listener.onEvent(new ReceiveEvent<>("test", 0, 2L, 0L, null, new PayloadOrError<>(null, new RuntimeException("ON PURPOSE"), "unit test error event")));
    assertThat(mockDao.getCount()).isZero();
  }

  @Test
  void testOnElectionVoteEventOnException() {
    final ElectionVote electionVote = new ElectionVote(UUID.randomUUID().toString(), "foo");
    final CloudEvent ce = new CloudEventBuilder()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create("http://test"))
            .withSubject("TEST")
            .withType(CloudEventTypes.ELECTION_VOTE_EVENT)
            .withData(StreamUtils.wrapCloudEventData(electionVote))
            .build();
    mockDao.throwOnNextInvoke();

    Assertions.assertDoesNotThrow(() -> listener.onEvent(new ReceiveEvent<>("test", 0, 3L, 0L, null, new PayloadOrError<>(ce, null, "unit test event"))));
  }


  static class TestDao implements ElectionDao {
    private int count = 0;
    private boolean throwException = false;

    public int getCount() {
      return count;
    }
    public void throwOnNextInvoke() { throwException = true; }

    @Override
    public Election updateElection(ElectionVote electionVote) {
      if (throwException) throw new RuntimeException("ON PURPOSE");
      count++;
      final String mocked = "mocked";
      return new Election(mocked, mocked, mocked, mocked, mocked, null);
    }

    /** Not being tested */
    @Override
    public Election insertElection(Election election) {return null;}
  }
}