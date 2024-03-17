package io.voting.streams.electionttl.topology.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.streams.electionttl.model.Heartbeat;
import io.voting.streams.electionttl.topology.TestCEMapper;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HeartbeatMapperTest {

  private ValueMapper<CloudEvent, Heartbeat> mapper = new HeartbeatMapper();

  public static Stream<Arguments> testElectionCreate() {
    return Stream.of(
            Arguments.of(UUID.randomUUID().toString()),
            Arguments.of(UUID.randomUUID().toString()),
            Arguments.of(UUID.randomUUID().toString()),
            Arguments.of(UUID.randomUUID().toString()),
            Arguments.of(UUID.randomUUID().toString()),
            Arguments.of(UUID.randomUUID().toString())
    );
  }

  @ParameterizedTest
  @MethodSource
  void testElectionCreate(final String eId) {
    final CloudEvent event = TestCEMapper.buildElectionCreate(eId);

    final Heartbeat actual = mapper.apply(event);

    assertThat(actual).isEqualTo(new Heartbeat(eId, CloudEventTypes.ELECTION_CREATE_EVENT));
  }

  public static Stream<Arguments> testElectionVote() {
    return Stream.of(
            Arguments.of("001", UUID.randomUUID().toString()),
            Arguments.of("002", UUID.randomUUID().toString()),
            Arguments.of("003", UUID.randomUUID().toString()),
            Arguments.of("004", UUID.randomUUID().toString()),
            Arguments.of("005", UUID.randomUUID().toString()),
            Arguments.of("006", UUID.randomUUID().toString())
    );
  }


  @ParameterizedTest
  @MethodSource
  void testElectionVote(final String uId, final String eId) {
    final CloudEvent event = TestCEMapper.buildElectionVote(uId, eId);

    final Heartbeat actual = mapper.apply(event);

    assertThat(actual).isEqualTo(new Heartbeat(uId, CloudEventTypes.ELECTION_VOTE_EVENT));
  }

}