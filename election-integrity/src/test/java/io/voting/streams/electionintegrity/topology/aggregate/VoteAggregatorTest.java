package io.voting.streams.electionintegrity.topology.aggregate;

import io.voting.common.library.models.ElectionVote;
import io.voting.streams.electionintegrity.model.ElectionSummary;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VoteAggregatorTest {
  static final String USER_ID = "001";
  static final String ELECTION_ID = "100";
  static final String KEY = USER_ID + ":" + ELECTION_ID;

  final Aggregator<String, ElectionVote, ElectionSummary> aggregator = new VoteAggregator();

  @Test
  void testFirstVote() {
    final ElectionSummary summary = new ElectionSummary();
    final ElectionVote vote = ElectionVote.of(ELECTION_ID, "Foo");

    final ElectionSummary applied = aggregator.apply(KEY, vote, summary);

    assertThat(applied.getVoteAttempts()).isEqualTo(1L);
    assertThat(applied.getVote()).isEqualTo(vote);
    assertThat(applied.getUser()).isEqualTo(USER_ID);
  }

  @Test
  void testDuplicateVote() {
    final ElectionSummary summary = new ElectionSummary();
    final ElectionVote vote = ElectionVote.of(ELECTION_ID, "Bar");
    summary.add(KEY, vote);

    final ElectionSummary applied = aggregator.apply(KEY, vote, summary);

    assertThat(applied.getVoteAttempts()).isEqualTo(2L);
    assertThat(applied.getVote()).isEqualTo(vote);
    assertThat(applied.getUser()).isEqualTo(USER_ID);
  }

  @Test
  void testInitializer() {
    final Initializer<ElectionSummary> initializer = VoteAggregator.initializer();
    final ElectionSummary init = initializer.apply();
    assertThat(init.getVote()).isNull();
    assertThat(init.getVoteAttempts()).isZero();
  }

  @Test
  void testMaterialize() {
    final Materialized<String, ElectionSummary, KeyValueStore<Bytes, byte[]>> materialize = VoteAggregator.materialize();
    assertThat(materialize).isNotNull();
  }
}