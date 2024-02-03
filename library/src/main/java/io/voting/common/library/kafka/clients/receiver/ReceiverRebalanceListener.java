package io.voting.common.library.kafka.clients.receiver;

import io.voting.common.library.kafka.models.PayloadOrError;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class ReceiverRebalanceListener<K, V> implements ConsumerRebalanceListener {

  private final Map<TopicPartition, OffsetAndMetadata> progress = new HashMap<>();
  private final Consumer<K, PayloadOrError<V>> consumer;

  public ReceiverRebalanceListener(final Consumer<K, PayloadOrError<V>> consumer) {
    this.consumer = consumer;
  }

  public Map<TopicPartition, OffsetAndMetadata> getCurrOffsets() {
    return progress;
  }

  public void addOffsetsToTrack(final String topic, int partition, long offset) {
    log.debug("Successfully processed event (topic, partition, offset) : ({}, {}, {})", topic, partition, offset);
    progress.put(
            new TopicPartition(topic, partition),
            new OffsetAndMetadata(offset+1, null)
    );
  }

  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> collection) {
    log.info("Receiver partitions revoked : committing consumed offsets for the following partitions (Revoked): {}", collection);
    for (final TopicPartition tp : collection) {
      log.debug("Committing offsets prior to revoke : {}", progress);
      if (Objects.isNull(progress.get(tp))) continue;
      consumer.commitSync(Map.of(tp, progress.get(tp)));
      progress.remove(tp);
    }
  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> collection) {
    log.info("Receiver partitions assigned : {} ", collection);
    if (collection.isEmpty()) return;
    final Set<TopicPartition> assignment = consumer.assignment();
    initOffset(assignment);
    log.info("Receiver subscription state post-partition-assignment : {}", progress);
  }

  private void initOffset(final Set<TopicPartition> assignment) {
    for (final TopicPartition tp : assignment) {
      addOffsetsToTrack(tp.topic(), tp.partition(), consumer.position(tp));
    }
  }
}
