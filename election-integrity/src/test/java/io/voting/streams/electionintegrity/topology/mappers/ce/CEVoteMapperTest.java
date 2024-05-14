package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.electionintegrity.model.ElectionSummary;
import io.voting.streams.electionintegrity.topology.ElectionIntegrityTopology;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CEVoteMapperTest {
  final String userId = "100";
  final String electionId = "000";
  final String key = userId + ":" + electionId;
  final ValueMapper<ElectionSummary, CloudEvent> mapper = new CEVoteMapper();
  ElectionSummary summary;

  @BeforeEach
  void setup() {
    summary = new ElectionSummary();
  }

  @Test
  void test() {
    final ElectionVote vote = ElectionVote.of(electionId, "Foo");
    summary.add(key, vote);

    final CloudEvent actual = mapper.apply(summary);

    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isEqualTo(userId);
    assertThat(actual.getSource().toString()).contains(ElectionIntegrityTopology.class.getSimpleName());
    assertThat(actual.getType()).isEqualTo(CloudEventTypes.ELECTION_VOTE_EVENT);
    assertThat(actual.getSubject()).isEqualTo(vote.getElectionId());
    assertThat(StreamUtils.unwrapCloudEventData(actual.getData(), ElectionVote.class))
            .isNotNull()
            .isEqualTo(vote);
  }
}
