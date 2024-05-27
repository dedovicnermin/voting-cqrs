package io.voting.streams.electionintegrity.topology.util;

import org.javatuples.Pair;

import java.time.Duration;
import java.time.Instant;

public final class TTLPairs {

  private TTLPairs() {}

  public static Pair<Instant, Instant> now(final Duration duration) {
    final Instant begin = Instant.now();
    final Instant end = begin.plus(duration);
    return Pair.with(begin, end);
  }
}
