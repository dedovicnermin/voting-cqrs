package io.voting.command.cmdbridge.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionVote;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VoteCmdMapper implements CloudEventMapper<String, ElectionVote> {

  @Override
  public CloudEvent apply(String key, ElectionVote electionVote) {
    log.trace("Applying transformation (K,V): {}, {}", key, electionVote);
    final CloudEvent event = ceBuilder
            .withId(key)
            .withType(CloudEventTypes.ELECTION_VOTE_CMD)
            .withSubject(electionVote.getElectionId())
            .withData(StreamUtils.wrapCloudEventData(electionVote))
            .build();
    log.trace("Applied transformation: {}", event);
    return event;
  }

}
