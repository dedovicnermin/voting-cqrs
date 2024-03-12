package io.voting.streams.electionttl.topology.aggregator;

import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.streams.electionttl.model.Heartbeat;
import io.voting.streams.electionttl.model.TTLSummary;
import org.apache.kafka.streams.kstream.Aggregator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TTLAggregatorTest {

  static final String ELECTION_ID = "777";
  static final String USER_ID = "000";
  final Aggregator<String, Heartbeat, TTLSummary> aggregator = new TTLAggregator();

  @Test
  void testInit() {
    final TTLSummary summary = TTLAggregator.initializer().apply();

    assertThat(summary).isNotNull();
    assertThat(summary.getElectionId()).isNull();
    assertThat(summary.getVoterList())
            .isNotNull()
            .isEmpty();

  }

  @Test
  void testElectionCreate() {
    final TTLSummary ttlSummary = new TTLSummary();

    aggregator.apply(
            ELECTION_ID,
            new Heartbeat(ELECTION_ID, CloudEventTypes.ELECTION_CREATE_EVENT),
            ttlSummary
    );

    assertThat(ttlSummary.getElectionId()).isEqualTo(ELECTION_ID);
    assertThat(ttlSummary.getVoterList()).isEmpty();
  }

  @Test
  void testElectionVote() {
    final TTLSummary ttlSummary = new TTLSummary();

    aggregator.apply(
            ELECTION_ID,
            new Heartbeat(USER_ID, CloudEventTypes.ELECTION_VOTE_EVENT),
            ttlSummary
    );

    assertThat(ttlSummary.getElectionId()).isEqualTo(ELECTION_ID);
    assertThat(ttlSummary.getVoterList()).hasSize(1)
            .contains(USER_ID);

  }

}