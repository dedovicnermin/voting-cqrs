package io.voting.common.library.kafka.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import io.cloudevents.kafka.CloudEventDeserializer;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.voting.common.library.kafka.clients.serialization.avro.KafkaAvroCloudEventDeserializer;
import io.voting.common.library.kafka.clients.serialization.avro.KafkaAvroCloudEventSerializer;
import io.voting.common.library.kafka.clients.serialization.ce.CESerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public final class StreamUtils {

  private StreamUtils() {}

  private static final ObjectMapper mapper = new ObjectMapper();

  public static <T> Serde<T> getJsonSerde(Class<T> tClass) {
    final JsonSerializer<T> serializer = new JsonSerializer<>();
    final JsonDeserializer<T> deserializer = new JsonDeserializer<>(tClass);
    return Serdes.serdeFrom(serializer,deserializer);
  }

  public static Serde<Object> getAvroCESerde() {
    final KafkaAvroCloudEventSerializer serializer = new KafkaAvroCloudEventSerializer();
    final KafkaAvroCloudEventDeserializer deserializer = new KafkaAvroCloudEventDeserializer();
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<Object> getAvroCESerde(final Properties properties) {
    final Map<String, Object> config = propertiesToMap(properties);
    return getAvroCESerde(config);
  }

  public static Serde<Object> getAvroCESerde(final Map<String, Object> config) {
    final KafkaAvroCloudEventSerializer serializer = new KafkaAvroCloudEventSerializer();
    final KafkaAvroCloudEventDeserializer deserializer = new KafkaAvroCloudEventDeserializer();
    serializer.configure(config, false);
    deserializer.configure(config, false);

    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<Object> getAvroCESerde(final SchemaRegistryClient srClient, final Properties properties) {
    final Map<String, Object> map = propertiesToMap(properties);
    final KafkaAvroCloudEventSerializer serializer = new KafkaAvroCloudEventSerializer(srClient);
    final KafkaAvroCloudEventDeserializer deserializer = new KafkaAvroCloudEventDeserializer(srClient);
    serializer.configure(map, false);
    deserializer.configure(map, false);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static void loadConfigFromFile(final String file, final Properties properties) {
    try (
            final FileInputStream inputStream = new FileInputStream(file)
    ) {
      properties.load(inputStream);
    } catch (IOException e) {
      log.error("Unable to load file ({}) into properties : {}", file, e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public static Map<String,Object> propertiesToMap(final Properties properties) {
    final Map<String, Object> configs = new HashMap<>();
    properties.forEach(
            (key, value) -> configs.put((String) key, value)
    );
    return configs;
  }

  @Deprecated
  public static Serde<CloudEvent> getCESerde() {
    final Serializer<CloudEvent> serializer = new CESerializer();
    final Deserializer<CloudEvent> deserializer = new CloudEventDeserializer();
    return Serdes.serdeFrom(serializer, deserializer);
  }

  @Deprecated
  public static <T> PojoCloudEventData<T> wrapCloudEventData(final T data) {
    return PojoCloudEventData.wrap(data, mapper::writeValueAsBytes);
  }

  @Deprecated
  public static <T> T unwrapCloudEventData(final CloudEventData data, final Class<T> target) {
    return PojoCloudEventDataMapper.from(mapper, target).map(data).getValue();
  }
}
