package io.voting.common.library.kafka.clients.serialization.avro;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.Encoding;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.entities.Metadata;
import io.confluent.kafka.schemaregistry.client.rest.entities.RuleSet;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaEntity;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.strategy.SubjectNameStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.IndexedRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class KafkaAvroCloudEventSerializer extends KafkaAvroSerializer {

  public static final String DATA_SCHEMA_HEADER = "ce_dataschema";

  private final CloudEventSerializer serializer = new CloudEventSerializer();
  private String schemaRegistryUrl;

  public KafkaAvroCloudEventSerializer() {}
  public KafkaAvroCloudEventSerializer(SchemaRegistryClient client) {
    super(client);
  }

  private Encoding encodingOf(Map<String, ?> configs){
    log.debug("Serializer config: {}", configs);

    Object encodingConfig = configs.get(CloudEventSerializer.ENCODING_CONFIG);
    Encoding encoding = null;

    if (encodingConfig instanceof String) {
      encoding = Encoding.valueOf((String) encodingConfig);
    } else if (encodingConfig instanceof Encoding) {
      encoding = (Encoding) encodingConfig;
    } else if (encodingConfig != null) {
      throw new IllegalArgumentException(CloudEventSerializer.ENCODING_CONFIG + " can be of type String or " + Encoding.class.getCanonicalName());
    }

    return encoding;
  }

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    final Encoding encoding = encodingOf(configs);

    if(encoding == Encoding.BINARY){
      super.configure(configs, isKey);
      serializer.configure(configs, isKey);
      schemaRegistryUrl = (String) configs.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG);

      log.debug("{}={}", AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    } else {
      throw new IllegalArgumentException(CloudEventSerializer.ENCODING_CONFIG + "=" + encoding + " not supported");
    }
  }

  @Override
  public byte[] serialize(String topic, Headers headers, Object event) {
    if( !(event instanceof CloudEvent ceEvent)){
      throw new IllegalArgumentException("event argument must be an instance of " + CloudEvent.class);
    }

    serializer.serialize(topic, headers, ceEvent);
    log.debug("CloudEvent headers {}", headers);


    if(ceEvent.getData() instanceof AvroCloudEventData<?> data) {
      final IndexedRecord value = data.getValue();
      final Class<? extends IndexedRecord> valueType = value.getClass();
      log.debug("Payload to serialize as avro {}", value);

      final byte[] bytes = super.serialize(topic, headers, data.getValue());

      final SubjectNameStrategy strategy = (SubjectNameStrategy) super.valueSubjectNameStrategy;
      log.debug("SubjectNameStrategy {}", strategy);

      final String subjectName = strategy.subjectName(
              topic,
              Boolean.FALSE,
              new NoSchema(valueType.getPackageName(), valueType.getSimpleName())
      );
      log.info("SubjectName {}", subjectName);

      try {
        final List<Integer> versions = super.schemaRegistry.getAllVersions(subjectName);
        final Integer version = versions.get(versions.size() - 1);
        log.debug("Schema versionId {}", version);

        final String dataschema = schemaRegistryUrl + "/subjects/" + subjectName + "/versions/" + version + "/schema";
        log.debug("{}={}", DATA_SCHEMA_HEADER, dataschema);

        headers.remove(DATA_SCHEMA_HEADER);
        headers.add(DATA_SCHEMA_HEADER, dataschema.getBytes());

      }catch(IOException | RestClientException e){
        throw new SerializationException(e.getMessage(), e);
      }

      return bytes;

    } else {
      throw new IllegalArgumentException("CloudEvent data attribute must be an instance of "
              + AvroCloudEventData.class.getName());
    }
  }

  private static final class NoSchema implements ParsedSchema {

    private final String namespace;
    private final String name;

    public NoSchema(String namespace, String name) {
      this.namespace = Objects.requireNonNull(namespace);
      this.name = Objects.requireNonNull(name);
    }

    @Override
    public String canonicalString() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Integer version() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String name() {
      return namespace + "." + name;
    }

    @Override
    public Object rawSchema() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<SchemaReference> references() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Metadata metadata() {
      throw new UnsupportedOperationException();
    }

    @Override
    public RuleSet ruleSet() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ParsedSchema copy() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ParsedSchema copy(Integer integer) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ParsedSchema copy(Metadata metadata, RuleSet ruleSet) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ParsedSchema copy(Map<SchemaEntity, Set<String>> map, Map<SchemaEntity, Set<String>> map1) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> isBackwardCompatible(ParsedSchema parsedSchema) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String schemaType() {
      throw new UnsupportedOperationException();
    }

  }
}
