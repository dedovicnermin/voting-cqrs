package io.voting.common.library.kafka.clients.serialization.avro;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.kafka.CloudEventDeserializer;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.header.Headers;

import java.util.Map;


@Slf4j
public class KafkaAvroCloudEventDeserializer extends KafkaAvroDeserializer {

    private final CloudEventDeserializer deserializer = new CloudEventDeserializer();

    public KafkaAvroCloudEventDeserializer() {}
    public KafkaAvroCloudEventDeserializer(SchemaRegistryClient registry) {
        super(registry);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        log.debug("Deserializer config: {}", configs);
        super.configure(configs, isKey);
        deserializer.configure(configs, isKey);
    }

    @Override
    public CloudEvent deserialize(String topic, Headers headers, byte[] bytes) {
        final Object value = super.deserialize(topic, headers, bytes);
        final AvroCloudEventData<GenericRecord> data = new AvroCloudEventData<>((GenericRecord) value);
        final CloudEvent event = deserializer.deserialize(topic, headers, bytes);
        return CloudEventBuilder
            .from(event)
            .withData(AvroCloudEventData.MIME_TYPE, data)
            .build();
    }
}
