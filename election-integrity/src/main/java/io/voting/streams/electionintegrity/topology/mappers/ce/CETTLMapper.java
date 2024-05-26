package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.events.cmd.ViewElection;
import io.voting.events.integrity.CloseElection;
import io.voting.events.integrity.IntegrityEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Named;

import java.time.OffsetDateTime;

@Slf4j
public class CETTLMapper implements CEMapper<ViewElection> {
  @Override
  public CloudEvent apply(ViewElection ce) {
    log.trace("Mapping closable-election ({}) into CloudEvent format", ce.getEId());
    final CloudEvent event = ceBuilder
            .withType(CloseElection.class.getName())
            .withId(ce.getEId().toString())
            .withSubject(ce.getEId().toString())
            .withData(AvroCloudEventData.MIME_TYPE, avroData(ce))
            .withTime(OffsetDateTime.now())
            .build();
    log.debug("Transformed CE Type {} : {}", CloseElection.class.getName(), event);
    return event;
  }

  public static Named name() {
    return Named.as("ttl.ce.mapper");
  }

  @Override
  public IntegrityEvent format(ViewElection value) {
    return new IntegrityEvent(
            CloseElection.newBuilder()
                    .setEId(value.getEId())
                    .build()
    );
  }
}
