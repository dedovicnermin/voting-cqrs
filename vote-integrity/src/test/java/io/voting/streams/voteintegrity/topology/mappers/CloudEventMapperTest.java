package io.voting.streams.voteintegrity.topology.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.voteintegrity.model.ElectionSummary;
import io.voting.streams.voteintegrity.topology.VoteIntegrityTopology;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CloudEventMapperTest {

  final ValueMapper<ElectionSummary, CloudEvent> mapper = new CloudEventMapper();
  ElectionSummary summary;

  @BeforeEach
  void setup() {
    summary = new ElectionSummary();
  }

  @Test
  void test() {
    final ElectionVote vote = ElectionVote.of("000", "Foo");
    summary.add(vote);

    final CloudEvent actual = mapper.apply(summary);

    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isNotNull();
    assertThat(actual.getSource().toString()).contains(VoteIntegrityTopology.class.getSimpleName());
    assertThat(actual.getType()).isEqualTo(CloudEventTypes.ELECTION_VOTE_EVENT);
    assertThat(actual.getSubject()).isEqualTo(vote.getElectionId());
    assertThat(StreamUtils.unwrapCloudEventData(actual.getData(), ElectionVote.class))
            .isNotNull()
            .isEqualTo(vote);
  }
}
