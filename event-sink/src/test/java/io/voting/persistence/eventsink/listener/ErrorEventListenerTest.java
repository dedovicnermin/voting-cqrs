package io.voting.persistence.eventsink.listener;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.persistence.eventsink.framework.TestReceiver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorEventListenerTest {

  private ErrorEventListener listener;

  @BeforeEach
  void setup() {
    listener = new ErrorEventListener(new TestReceiver());
  }

  @Test
  void testOnErrorEvent() {
    final ReceiveEvent<String, CloudEvent> receiveEvent = new ReceiveEvent<>("test", 0, 0L, 0L, null, new PayloadOrError<>(null, new RuntimeException(), "error event for unit test"));

    listener.onEvent(receiveEvent);

    assertThat(listener.getErrorCount()).isOne();
    assertThat(listener.getErrors().get(0)).isEqualTo(receiveEvent);
  }

  @Test
  void testOnGoodEvent() {
    final CloudEvent ce = new CloudEventBuilder()
            .withId(UUID.randomUUID().toString())
            .withSource(URI.create("http://test"))
            .withSubject("TEST")
            .withType(CloudEventTypes.ELECTION_VOTE_EVENT)
            .build();
    final ReceiveEvent<String, CloudEvent> receiveEvent = new ReceiveEvent<>("test", 0, 0L, 0L, null, new PayloadOrError<>(ce, null, ""));

    listener.onEvent(receiveEvent);

    assertThat(listener.getErrorCount()).isZero();
  }

}