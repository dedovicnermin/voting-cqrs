package io.voting.common.library.kafka.clients.serialization.ce;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventSerializer;
import org.apache.kafka.common.header.internals.RecordHeaders;

@Deprecated
public class CESerializer extends CloudEventSerializer {

  public CESerializer() {
    super();
  }

  @Override
  public byte[] serialize(String topic, CloudEvent data) {
    return serialize(topic, new RecordHeaders(), data);

  }
}
