package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewElection;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Named;

import java.time.OffsetDateTime;

@Slf4j
public class CEElectionMapper implements CEMapper<NewElection> {

  @Override
  public CloudEvent apply(NewElection election) {
    log.trace("Mapping election ({}) into CloudEvent format", election);
    final CloudEvent event = ceBuilder
            .withId(election.getId().toString())
            .withType(NewElection.class.getName())
            .withSubject(election.getCategory().toString())
            .withData(AvroCloudEventData.MIME_TYPE, avroData(election))
            .withTime(OffsetDateTime.now())
            .build();
    log.trace("CE result : {}", event);
    return event;
  }

  public static Named name() {
    return Named.as("ei.ce.mapper");
  }

  @Override
  public IntegrityEvent format(NewElection election) {
    return new IntegrityEvent(election);
  }
}
