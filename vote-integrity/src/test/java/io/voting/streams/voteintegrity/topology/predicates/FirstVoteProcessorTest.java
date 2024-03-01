package io.voting.streams.voteintegrity.topology.predicates;

import io.voting.common.library.models.ElectionVote;
import io.voting.streams.voteintegrity.model.ElectionSummary;
import org.apache.kafka.streams.kstream.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FirstVoteProcessorTest {

  static final String KEY = "001:001";
  static final ElectionVote VOTE = ElectionVote.of("001", "TEST");

  final Predicate<String, ElectionSummary> processor = new FirstVoteProcessor();
  ElectionSummary summary;

  @BeforeEach
  void setup() {
    summary = new ElectionSummary();
  }

  @Test
  void testFirstVote() {
    summary.add(VOTE);
    assertThat(processor.test(KEY, summary)).isTrue();
  }

  @Test
  void testDuplicateVote() {
    summary.add(VOTE);
    summary.add(VOTE);
    assertThat(processor.test(KEY, summary)).isFalse();
  }

  @Test
  void testUnexpectedIllegalVote() {
    summary.add(VOTE);
    summary.setVoteAttempts(0L);
    assertThat(processor.test(KEY, summary)).isFalse();
  }
}
