package io.voting.streams.electionttl.topology;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.streams.electionttl.model.Heartbeat;
import io.voting.streams.electionttl.model.TTLSummary;
import io.voting.streams.electionttl.topology.mappers.CloudEventMapper;

import java.net.URI;

public class TestCEMapper {

  private static final CloudEventBuilder ceBuilder = new CloudEventBuilder()
          .newBuilder()
          .withSource(URI.create("https://" + TestCEMapper.class.getSimpleName()));

  public static CloudEvent buildElectionCreate(final String eId) {
    return ceBuilder
            .withId(eId)
            .withType(CloudEventTypes.ELECTION_CREATE_EVENT)
            .withSubject(eId)
            .build();
  }

  public static CloudEvent buildElectionVote(final String userId, final String eId) {
    return ceBuilder
            .withId(userId)
            .withType(CloudEventTypes.ELECTION_VOTE_EVENT)
            .withSubject(eId)
            .build();
  }

  public static CloudEvent buildExpectedTTLEvent(final String eId) {
    return new CloudEventMapper()
            .apply(
                    new TTLSummary().add(eId, new Heartbeat(eId, null))
            );
  }


}
