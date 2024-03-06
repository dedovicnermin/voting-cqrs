package io.voting.streams.electionttl.topology.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.streams.electionttl.model.Heartbeat;
import io.voting.streams.electionttl.model.TTLSummary;
import io.voting.streams.electionttl.topology.TTLTopology;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CloudEventMapperTest {

  private ValueMapper<TTLSummary, CloudEvent>  mapper = new CloudEventMapper();

  public static Stream<Arguments> test() {
    return Stream.of(
            Arguments.of(UUID.randomUUID().toString()),
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
  void test(final String electionId) {
    final TTLSummary electionTTLSummary = new TTLSummary()
            .add(electionId, new Heartbeat(electionId, CloudEventTypes.ELECTION_CREATE_EVENT))
            .add(electionId, new Heartbeat("001", CloudEventTypes.ELECTION_VOTE_EVENT))
            .add(electionId, new Heartbeat("002", CloudEventTypes.ELECTION_VOTE_EVENT))
            .add(electionId, new Heartbeat("003", CloudEventTypes.ELECTION_VOTE_EVENT))
            .add(electionId, new Heartbeat("004", CloudEventTypes.ELECTION_VOTE_EVENT))
            .add(electionId, new Heartbeat("005", CloudEventTypes.ELECTION_VOTE_EVENT));

    final CloudEvent actual = mapper.apply(electionTTLSummary);

    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isEqualTo(electionId);
    assertThat(actual.getSubject()).isEqualTo(electionId);
    assertThat(actual.getSource().toString()).contains(TTLTopology.class.getSimpleName());
    assertThat(actual.getType()).isEqualTo(CloudEventTypes.ELECTION_EXPIRATION_EVENT);
    assertThat(new String(Objects.requireNonNull(actual.getData()).toBytes())).isEqualTo(electionId);
  }

}