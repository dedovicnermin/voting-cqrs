package io.voting.common.library.kafka.clients.serialization.ce;


import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.kafka.CloudEventDeserializer;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.models.PayloadOrError;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.header.Headers;

import java.util.Map;

@Slf4j
public class AvroCEPayloadDeserializer extends KafkaAvroDeserializer {

  private final CloudEventDeserializer deserializer = new CloudEventDeserializer();

  public AvroCEPayloadDeserializer() {}
  public AvroCEPayloadDeserializer(SchemaRegistryClient registry) {
    super(registry);
  }

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    log.debug("Deserializer config: {}", configs);
    super.configure(configs, isKey);
    deserializer.configure(configs, isKey);
  }

  @Override
  public PayloadOrError<CloudEvent> deserialize(String topic, Headers headers, byte[] data) {
    final String encoded = new String(data);
    log.debug("Attempting to deserialize value (encoded) : {}", encoded);

    try {
      final Object dataValue = super.deserialize(topic, headers, data);
      final AvroCloudEventData<GenericRecord> ceData = new AvroCloudEventData<>((GenericRecord) dataValue);
      final CloudEvent event = deserializer.deserialize(topic, headers, data);
      return new PayloadOrError<>(
              CloudEventBuilder
                      .from(event)
                      .withData(AvroCloudEventData.MIME_TYPE, ceData)
                      .build(),
              null,
              encoded
      );
    } catch (Exception e) {
      log.error("Error deserializing payload ({}) : encoded value {}", topic, encoded, e);
      return new PayloadOrError<>(null, e, encoded);
    }
  }
}
