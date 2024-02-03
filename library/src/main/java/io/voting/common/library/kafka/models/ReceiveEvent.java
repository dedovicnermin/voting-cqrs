package io.voting.common.library.kafka.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ReceiveEvent<K, V> {
  private final String topic;
  private final Integer partition;
  private final Long offset;
  private final Long timestamp;
  private final K key;
  private final PayloadOrError<V> pOrE;

}
