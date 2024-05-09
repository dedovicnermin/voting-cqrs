package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CEElectionMapper implements CEMapper<Election> {

  @Override
  public CloudEvent apply(Election election) {
    log.trace("Mapping election ({}) into CloudEvent format", election);
    final CloudEvent event = ceBuilder
            .withId(election.getId())
            .withType(CloudEventTypes.ELECTION_CREATE_EVENT)
            .withSubject(election.getCategory())
            .withData(StreamUtils.wrapCloudEventData(election))
            .build();
    log.trace("CE result : {}", event);
    return event;
  }
}
