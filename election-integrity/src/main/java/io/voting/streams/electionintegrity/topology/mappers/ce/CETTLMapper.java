package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.streams.electionintegrity.model.TTLSummary;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CETTLMapper implements CEMapper<TTLSummary> {
  @Override
  public CloudEvent apply(TTLSummary ttlSummary) {
    log.trace("Mapping TTLSummary ({}) into CloudEvent format", ttlSummary);
    final CloudEvent event = ceBuilder
            .withType(CloudEventTypes.ELECTION_EXPIRATION_EVENT)
            .withId(ttlSummary.getElectionId())
            .withSubject(ttlSummary.getElectionId())
            .withData(ttlSummary.getElectionId().getBytes())
            .build();
    log.debug("Transformed CE Type {} : {}", CloudEventTypes.ELECTION_EXPIRATION_EVENT, event);
    return event;
  }
}
