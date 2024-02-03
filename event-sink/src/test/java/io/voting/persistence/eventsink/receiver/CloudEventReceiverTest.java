package io.voting.persistence.eventsink.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.clients.receiver.EventReceiver;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.models.ElectionVote;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

class CloudEventReceiverTest {

  private static final String TOPIC = "test.topic";
  private static final String ELECTION_ID = "000";

  private final ObjectMapper mapper = new ObjectMapper();

  private MockConsumer<String, PayloadOrError<CloudEvent>> consumer;
  private EventReceiver<String, CloudEvent> receiver;
  private TestListener listener;

  @BeforeEach
  void setup() {
    consumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
    receiver = new CloudEventReceiver(consumer, Collections.singletonList(TOPIC), Duration.ofMillis(1000L));
    listener = new TestListener(receiver);
  }

  @Test
  void testFireEvent() {
    consumer.schedulePollTask(() -> {
      consumer.rebalance(Collections.singletonList(new TopicPartition(TOPIC, 0)));
      consumer.addRecord(new ConsumerRecord<>(TOPIC, 0, 0, "001", electionVoteEvent()));
    });
    consumer.schedulePollTask(() -> receiver.close());
    consumer.updateBeginningOffsets(Map.of(new TopicPartition(TOPIC, 0), 0L));

    try {
      receiver.start();
    } catch (WakeupException e) {
      // do nothing
    } finally {
      Assertions.assertThat(listener.getCount()).isEqualTo(1);
    }
  }

  private PayloadOrError<CloudEvent> electionVoteEvent() {
    final ElectionVote vote = ElectionVote.of(ELECTION_ID, "FOO");
    final CloudEvent event = new CloudEventBuilder()
            .newBuilder()
            .withSource(URI.create("https://unit.test"))
            .withType(CloudEventTypes.ELECTION_VOTE_EVENT)
            .withId(UUID.randomUUID().toString())
            .withSubject(ELECTION_ID)
            .withData(PojoCloudEventData.wrap(vote, mapper::writeValueAsBytes))
            .build();
    return new PayloadOrError<>(event, null, "");
  }

  static class TestListener implements EventListener<String, CloudEvent> {

    @Getter
    private Long count = 0L;

    public TestListener(EventReceiver<String, CloudEvent> receiver) {
      receiver.addListener(this);
    }

    @Override
    public void onEvent(ReceiveEvent<String, CloudEvent> event) {
      count++;
    }
  }

}