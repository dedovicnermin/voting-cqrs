package io.voting.persistence.eventsink.receiver;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.receiver.AbstractEventReceiver;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;
import java.util.List;

@Slf4j
public class CloudEventReceiver extends AbstractEventReceiver<String, CloudEvent> {

  private final List<String> topics;
  private final Duration duration;

  public CloudEventReceiver(final Consumer<String, PayloadOrError<CloudEvent>> consumer, final List<String> topics, final Duration duration) {
    super(consumer);
    this.topics = topics;
    this.duration = duration;
  }

  @Override
  public void start() {
    log.info("Subscribing to topics : {}", topics);
    consumer.subscribe(topics, rebalanceListener);
    while (receiverIsRunning()) {
      final ConsumerRecords<String, PayloadOrError<CloudEvent>> cRecords = consumer.poll(duration);
      if (cRecords.isEmpty()) continue;
      for (final ConsumerRecord<String, PayloadOrError<CloudEvent>> cRecord : cRecords) {
        log.trace("Received event record : {}", cRecord);
        final ReceiveEvent<String, CloudEvent> receiveEvent = new ReceiveEvent<>(
                cRecord.topic(), cRecord.partition(), cRecord.offset(), cRecord.timestamp(), cRecord.key(), cRecord.value()
        );
        fire(receiveEvent);
        rebalanceListener.addOffsetsToTrack(cRecord.topic(), cRecord.partition(), cRecord.offset());
      }
      consumer.commitAsync();
    }
  }
}
