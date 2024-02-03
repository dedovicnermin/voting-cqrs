package io.voting.common.library.kafka.clients.serialization.ce;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventDeserializer;
import io.voting.common.library.kafka.models.PayloadOrError;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

@Slf4j
public class CEPayloadDeserializer implements Deserializer<PayloadOrError<CloudEvent>> {

  private final CloudEventDeserializer deserializer;

  public CEPayloadDeserializer() {
    this.deserializer = new CloudEventDeserializer();
  }

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    deserializer.configure(configs, isKey);
  }

  @Override
  public PayloadOrError<CloudEvent> deserialize(String topic, byte[] data) {
    throw new UnsupportedOperationException("CloudEventDeserializer supports only the signature deserialize(String, Headers, byte[])");
  }

  @Override
  public PayloadOrError<CloudEvent> deserialize(String topic, Headers headers, byte[] data) {
    final String encoded = new String(data);
    try {
      log.debug("Attempting to deserialize value (encoded) : {}", encoded);
      return new PayloadOrError<>(deserializer.deserialize(topic, headers, data), null, encoded);
    } catch (Exception e) {
      log.error("Error deserializing payload ({}) : encoded value {}", topic, encoded, e);
      return new PayloadOrError<>(null, e, encoded);
    }
  }

}
