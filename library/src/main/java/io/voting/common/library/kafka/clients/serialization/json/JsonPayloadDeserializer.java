package io.voting.common.library.kafka.clients.serialization.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.voting.common.library.kafka.models.PayloadOrError;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Slf4j
public class JsonPayloadDeserializer<T> implements Deserializer<PayloadOrError<T>> {
    private final JsonDeserializer<T> deserializer;

    public JsonPayloadDeserializer(final ObjectMapper mapper, Class<T> clazz) {
        deserializer = new JsonDeserializer<>(clazz, mapper);
    }

    @Override
    public PayloadOrError<T> deserialize(String topic, byte[] bytes) {
        final String encoded = new String(bytes);
        try {
            log.debug("Attempting to deserialize value (encoded) : {}", encoded);
            return new PayloadOrError<>(deserializer.deserialize(topic, bytes), null, encoded);
        } catch (Exception e) {
            log.error("Error deserializing payload ({}) : {}", topic, e.getMessage());
            return new PayloadOrError<>(null, e, encoded);
        }
    }
}
