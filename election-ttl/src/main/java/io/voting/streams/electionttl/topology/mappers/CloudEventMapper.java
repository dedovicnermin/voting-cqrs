package io.voting.streams.electionttl.topology.mappers;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.streams.electionttl.model.TTLSummary;
import io.voting.streams.electionttl.topology.TTLTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.net.URI;

@Slf4j
public class CloudEventMapper implements ValueMapper<TTLSummary, CloudEvent> {

  private static final CloudEventBuilder ceBuilder = new CloudEventBuilder()
          .newBuilder()
          .withSource(URI.create("https://" + TTLTopology.class.getSimpleName()))
          .withType(CloudEventTypes.ELECTION_EXPIRATION_EVENT);

  @Override
  public CloudEvent apply(TTLSummary ttlSummary) {
    log.trace("Mapping TTLSummary ({}) into CloudEvent format", ttlSummary);
    final CloudEvent event = ceBuilder
            .withId(ttlSummary.getElectionId())
            .withSubject(ttlSummary.getElectionId())
            .withData(ttlSummary.getElectionId().getBytes())
            .build();
    log.debug("{} before sending event : {}", CloudEventTypes.ELECTION_EXPIRATION_EVENT, event);
    return event;
  }
}
