package io.voting.streams.voteintegrity.topology.mappers;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.voteintegrity.model.ElectionSummary;
import io.voting.streams.voteintegrity.topology.VoteIntegrityTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.net.URI;
import java.util.UUID;

@Slf4j
public class CloudEventMapper implements ValueMapper<ElectionSummary, CloudEvent> {

  private static final CloudEventBuilder builder = new CloudEventBuilder()
          .newBuilder()
          .withSource(URI.create("https://" + VoteIntegrityTopology.class.getSimpleName()))
          .withType(CloudEventTypes.ELECTION_VOTE_EVENT);

  @Override
  public CloudEvent apply(ElectionSummary electionSummary) {
    log.debug("Transforming legal vote into CloudEvent format : {}", electionSummary);
    final ElectionVote vote = electionSummary.getVote();

    final CloudEvent event = builder
            .withId(UUID.randomUUID().toString())
            .withSubject(vote.getElectionId())
            .withData(StreamUtils.wrapCloudEventData(vote))
            .build();

    log.debug("Legal vote transformation result : {}", event);
    return event;
  }
}
