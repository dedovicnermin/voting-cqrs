package io.voting.streams.electionintegrity.framework;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionVote;

import java.net.URI;

public class TestCmdBuilder {

  private static final CloudEventBuilder ceBuilder = new CloudEventBuilder()
          .newBuilder()
          .withSource(URI.create("https://" + "TEST"));

  public static CloudEvent buildCE(final ElectionCreate electionCreate) {
    return ceBuilder.withId("test").withType(CloudEventTypes.ELECTION_CREATE_CMD).withSubject("test").withData(StreamUtils.wrapCloudEventData(electionCreate)).build();
  }

  private CloudEvent buildCE(final ElectionVote electionVote) {
    return ceBuilder.withId("test").withType(CloudEventTypes.ELECTION_VOTE_CMD).withSubject("test").withData(StreamUtils.wrapCloudEventData(electionVote)).build();
  }
}