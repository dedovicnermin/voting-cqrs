package io.voting.streams.electionintegrity.topology.joiner;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.models.ElectionView;
import io.voting.streams.electionintegrity.framework.TestCEBuilder;
import io.voting.streams.electionintegrity.model.ElectionHeartbeat;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionViewJoinerTest {

  private static final String electionId = "electionId001";
  private static final ValueJoiner<CloudEvent, ElectionView, ElectionHeartbeat> joiner = new ElectionViewJoiner();

  @ParameterizedTest
  @EnumSource(ElectionView.class)
  void testElectionViewInitiatedJoin(ElectionView view) {
    final CloudEvent event = TestCEBuilder.ceBuilder
            .withId(electionId)
            .withType(CloudEventTypes.ELECTION_VIEW_CMD)
            .withSubject(electionId)
            .withData(electionId.getBytes())
            .build();
    final ElectionHeartbeat actual = joiner.apply(event, view);

    assertThat(actual).isEqualTo(new ElectionHeartbeat(electionId, CloudEventTypes.ELECTION_VIEW_CMD, view));
  }

  @ParameterizedTest
  @EnumSource(ElectionView.class)
  void testElectionCreateInitiatedJoin(ElectionView view) {
    final CloudEvent event = TestCEBuilder.ceBuilder
            .withId(electionId)
            .withType(CloudEventTypes.ELECTION_CREATE_EVENT)
            .withSubject("SPORTS")
            .withData(electionId.getBytes())
            .build();
    final ElectionHeartbeat actual = joiner.apply(event, view);

    assertThat(actual).isEqualTo(new ElectionHeartbeat(electionId, CloudEventTypes.ELECTION_CREATE_EVENT, view));
  }

  @ParameterizedTest
  @EnumSource(ElectionView.class)
  void testElectionVoteInitiatedJoin(ElectionView view) {
    final CloudEvent event = TestCEBuilder.ceBuilder
            .withId("user001")
            .withType(CloudEventTypes.ELECTION_VOTE_EVENT)
            .withSubject(electionId)
            .withData(electionId.getBytes())
            .build();
    final ElectionHeartbeat actual = joiner.apply(event, view);

    assertThat(actual).isEqualTo(new ElectionHeartbeat("user001", CloudEventTypes.ELECTION_VOTE_EVENT, view));
  }

  @Test
  void testOnNull() {
    final CloudEvent event = TestCEBuilder.ceBuilder
            .withId(electionId)
            .withType(CloudEventTypes.ELECTION_CREATE_EVENT)
            .withSubject("SPORTS")
            .withData(electionId.getBytes())
            .build();
    final ElectionHeartbeat actual = joiner.apply(event, null);
    assertThat(actual).isEqualTo(new ElectionHeartbeat(electionId, CloudEventTypes.ELECTION_CREATE_EVENT, ElectionView.OPEN));
  }


}