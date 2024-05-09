package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.electionintegrity.model.ElectionSummary;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CEVoteMapper implements CEMapper<ElectionSummary> {
  @Override
  public CloudEvent apply(ElectionSummary electionSummary) {
    log.trace("Transforming legal vote into CloudEvent format : {}", electionSummary);
    final ElectionVote vote = electionSummary.getVote();

    final CloudEvent event = ceBuilder
            .withId(electionSummary.getUser())
            .withType(CloudEventTypes.ELECTION_VOTE_EVENT)
            .withSubject(vote.getElectionId())
            .withData(StreamUtils.wrapCloudEventData(vote))
            .build();

    log.debug("Legal vote transformation result : {}", event);
    return event;
  }
}
