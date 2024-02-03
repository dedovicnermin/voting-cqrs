package io.voting.common.library.kafka.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PayloadOrError<V> {
  private final V payload;
  private final Throwable error;
  private final String encodedValue;
}
