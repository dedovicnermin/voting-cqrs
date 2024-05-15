package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Named;

@Slf4j
public class CETTLMapper implements CEMapper<CloudEvent> {
  @Override
  public CloudEvent apply(CloudEvent ce) {
    log.trace("Mapping closable-election ({}) into CloudEvent format", ce.getId());
    final CloudEvent event = ceBuilder
            .withType(CloudEventTypes.ELECTION_EXPIRATION_EVENT)
            .withId(ce.getId())
            .withSubject(ce.getId())
            .withData(ce.getId().getBytes())
            .build();
    log.debug("Transformed CE Type {} : {}", CloudEventTypes.ELECTION_EXPIRATION_EVENT, event);
    return event;
  }

  public static Named name() {
    return Named.as("ttl.ce.mapper");
  }
}
