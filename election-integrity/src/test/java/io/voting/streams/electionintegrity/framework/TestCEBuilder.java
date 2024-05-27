package io.voting.streams.electionintegrity.framework;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionView;
import io.voting.common.library.models.ElectionVote;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.CreateElection;
import io.voting.events.cmd.RegisterVote;
import io.voting.events.cmd.ViewElection;

import java.net.URI;
import java.time.OffsetDateTime;

public class TestCEBuilder {

  public static final CloudEventBuilder ceBuilder = new CloudEventBuilder()
          .newBuilder()
          .withTime(OffsetDateTime.now())
          .withSource(URI.create("https://" + "TEST"));

  public static CloudEvent buildCE(final CreateElection electionCreate) {
    return ceBuilder.withId("test").withType(CreateElection.class.getName()).withSubject("test").withData(AvroCloudEventData.MIME_TYPE, new AvroCloudEventData<>(new CmdEvent(electionCreate))).build();
  }

  public static CloudEvent buildCE(final RegisterVote electionVote) {
    return ceBuilder.withId("test").withType(RegisterVote.class.getName()).withSubject("test").withData(AvroCloudEventData.MIME_TYPE, new AvroCloudEventData<>(new CmdEvent(electionVote))).build();
  }

  public static CloudEvent buildCE(final String key, final ViewElection viewElection) {
    return ceBuilder.withId(key).withType(ViewElection.class.getName()).withSubject(key).withData(AvroCloudEventData.MIME_TYPE, new AvroCloudEventData<>(new CmdEvent(viewElection))).build();
  }

  @Deprecated
  public static CloudEvent buildCE(final ElectionCreate electionCreate) {
    return ceBuilder.withId("test").withType(CloudEventTypes.ELECTION_CREATE_CMD).withSubject("test").withData(StreamUtils.wrapCloudEventData(electionCreate)).build();
  }

  @Deprecated
  public static CloudEvent buildCE(final ElectionVote electionVote) {
    return ceBuilder.withId("test").withType(CloudEventTypes.ELECTION_VOTE_CMD).withSubject("test").withData(StreamUtils.wrapCloudEventData(electionVote)).build();
  }

  @Deprecated
  public static CloudEvent buildCE(final String key, final ElectionView electionView) {
    return ceBuilder.withId(key).withType(CloudEventTypes.ELECTION_VIEW_CMD).withSubject(key).withData(StreamUtils.wrapCloudEventData(electionView)).build();
  }
}
