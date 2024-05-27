package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.models.ElectionVote;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewVote;
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

    final NewVote expected = NewVote.newBuilder().setEId(electionId).setUId(userId).setCandidate("Foo").build();

    final CloudEvent actual = mapper.apply(summary);

    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isEqualTo(userId);
    assertThat(actual.getSource().toString()).contains(ElectionIntegrityTopology.class.getSimpleName());
    assertThat(actual.getType()).isEqualTo(NewVote.class.getName());
    assertThat(actual.getSubject()).isEqualTo(vote.getElectionId());
    assertThat(AvroCloudEventData.<IntegrityEvent>dataOf(actual.getData()).getLegalEvent())
            .isNotNull()
            .isEqualTo(expected);
  }
}
