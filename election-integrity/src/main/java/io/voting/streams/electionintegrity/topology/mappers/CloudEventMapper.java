package io.voting.streams.electionintegrity.topology.mappers;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.streams.electionintegrity.topology.ElectionIntegrityTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.net.URI;
import java.util.UUID;

@Slf4j
public class CloudEventMapper implements ValueMapper<Election, CloudEvent> {

  private static final CloudEventBuilder ceBuilder = new CloudEventBuilder()
          .newBuilder()
          .withSource(URI.create("https://" + ElectionIntegrityTopology.class.getSimpleName()))
          .withType(CloudEventTypes.ELECTION_CREATE_EVENT);

  @Override
  public CloudEvent apply(Election election) {
    log.debug("Mapping election ({}) into CloudEvent format", election);
    final CloudEvent event = ceBuilder
            .withId(UUID.randomUUID().toString())
            .withSubject(election.getCategory())
            .withData(StreamUtils.wrapCloudEventData(election))
            .build();
    log.debug("Format result : {}", event);
    return event;
  }
}
