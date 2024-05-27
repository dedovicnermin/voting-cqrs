package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.streams.electionintegrity.topology.ElectionIntegrityTopology;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.net.URI;

public interface CEMapper<T> extends ValueMapper<T, CloudEvent> {

  CloudEventBuilder ceBuilder = new CloudEventBuilder()
          .newBuilder()
          .withSource(URI.create("https://" + ElectionIntegrityTopology.class.getSimpleName()));

  default AvroCloudEventData<IntegrityEvent> avroData(T value) {
    return new AvroCloudEventData<>(
            format(value)
    );
  }

  IntegrityEvent format(T value);

}
