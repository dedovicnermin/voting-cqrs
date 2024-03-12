package io.voting.streams.electionintegrity.topology.util;

import org.javatuples.Pair;

import java.time.Duration;
import java.time.Instant;

public final class TTLPairs {

  private TTLPairs() {}

  public static Pair<Long, Long> now(final Duration duration) {
    final Instant now = Instant.now();
    final long begin = now.toEpochMilli();
    final long end = now.plus(duration).toEpochMilli();
    return Pair.with(begin, end);
  }
}
