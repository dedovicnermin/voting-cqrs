package io.voting.common.library.kafka.clients.serialization.avro;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.RegisterVote;
import io.voting.events.cmd.ViewElection;
import io.voting.events.enums.ElectionView;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaAvroCloudEventDeserializerTest {

  private static final String TOPIC = "test";
  private static Map<String, Object> configs;

  @BeforeAll
  static void initConfigs() {
    configs = new HashMap<>(Map.of(
            CloudEventSerializer.ENCODING_CONFIG, "BINARY",
            KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "true",
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://mock:8081"
    ));
  }

  private SchemaRegistryClient srClient;
  private KafkaAvroCloudEventDeserializer deserializer;
  private KafkaAvroCloudEventSerializer serializer;

  @BeforeEach
  void setup() {
    srClient = new MockSchemaRegistryClient();
    serializer = new KafkaAvroCloudEventSerializer(srClient);
    deserializer = new KafkaAvroCloudEventDeserializer(srClient);
  }

  @AfterEach
  void remove() {
    configs.remove(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG);
    serializer.close();
    deserializer.close();
  }

  @Test
  void deserializeSpecificRecord() {
    configs.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
    serializer.configure(configs, false);
    deserializer.configure(configs, false);

    final CmdEvent cmdEvent = new CmdEvent(new RegisterVote("eid", "Foo"));
    final AvroCloudEventData<CmdEvent> ceData = new AvroCloudEventData<>(cmdEvent);
    final CloudEvent ce = CloudEventBuilder.v1()
            .withId("")
            .withSource(URI.create("/test"))
            .withType(cmdEvent.getCmd().getClass().getName())
            .withSubject("TEST")
            .withTime(OffsetDateTime.now())
            .withData(AvroCloudEventData.MIME_TYPE, ceData)
            .build();

    final Headers headers = new RecordHeaders();
    final byte[] bytes = serializer.serialize(TOPIC, headers, ce);

    final CloudEvent actualCe = deserializer.deserialize(TOPIC, headers, bytes);
    final CloudEventData actualCeData = actualCe.getData();
    assertTrue(actualCeData instanceof AvroCloudEventData);

    @SuppressWarnings("unchecked")
    final AvroCloudEventData<CmdEvent> actualAvroData = (AvroCloudEventData<CmdEvent>) actualCeData;
    final CmdEvent actualCmdEvent = actualAvroData.getValue();

    assertEquals(cmdEvent, actualCmdEvent);

  }

  @Test
  void deserializeGenericRecord() {
    configs.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "false");
    serializer.configure(configs, false);
    deserializer.configure(configs, false);

    final RegisterVote registerVote = new RegisterVote("eid", "Foo");
    final AvroCloudEventData<RegisterVote> ceData = new AvroCloudEventData<>(registerVote);
    final CloudEvent ce = CloudEventBuilder.v1()
            .withId("")
            .withSource(URI.create("/test"))
            .withType(registerVote.getClass().getName())
            .withSubject("TEST")
            .withTime(OffsetDateTime.now())
            .withData(AvroCloudEventData.MIME_TYPE, ceData)
            .build();

    final Headers headers = new RecordHeaders();
    final byte[] bytes = serializer.serialize(TOPIC, headers, ce);

    final CloudEvent actualCe = deserializer.deserialize(TOPIC, headers, bytes);
    final CloudEventData actualCeData = actualCe.getData();
    assertTrue(actualCeData instanceof AvroCloudEventData);

    @SuppressWarnings("unchecked")
    final AvroCloudEventData<GenericRecord> actualAvroData = (AvroCloudEventData<GenericRecord>) actualCeData;
    final GenericRecord actualRegisterVote = actualAvroData.getValue();

    assertEquals(registerVote.getEId(), actualRegisterVote.get("eId").toString());
    assertEquals(registerVote.getVotedFor(), actualRegisterVote.get("votedFor").toString());
  }

  @Test
  void deserializeCloudEventMetadata() {
    configs.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "false");
    serializer.configure(configs, false);
    deserializer.configure(configs, false);

    final CmdEvent cmdEvent = new CmdEvent(new ViewElection("eid", ElectionView.OPEN));
    final AvroCloudEventData<CmdEvent> ceData = new AvroCloudEventData<>(cmdEvent);
    final CloudEvent ce = CloudEventBuilder.v1()
            .withId("123")
            .withSource(URI.create("/test"))
            .withType(cmdEvent.getCmd().getClass().getName())
            .withSubject("TEST")
            .withTime(OffsetDateTime.now())
            .withData(AvroCloudEventData.MIME_TYPE, ceData)
            .build();

    final Headers headers = new RecordHeaders();
    final byte[] bytes = serializer.serialize(TOPIC, headers, ce);
    final CloudEvent actualCe = deserializer.deserialize(TOPIC, headers, bytes);

    assertEquals(ce.getId(), actualCe.getId());
    assertEquals(ce.getSource(), actualCe.getSource());
    assertEquals(ce.getType(), actualCe.getType());
    assertEquals(ce.getSubject(), actualCe.getSubject());
    assertEquals(ce.getTime(), actualCe.getTime());
    assertNotNull(actualCe.getDataSchema());


  }


}