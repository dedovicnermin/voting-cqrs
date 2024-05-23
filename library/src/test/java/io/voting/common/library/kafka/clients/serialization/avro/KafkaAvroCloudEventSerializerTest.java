package io.voting.common.library.kafka.clients.serialization.avro;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.ViewElection;
import io.voting.events.enums.ElectionView;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaAvroCloudEventSerializerTest {

  @Test
  void incompatibleEncoding() {
    final Map<String, String> config = Map.of(
            CloudEventSerializer.ENCODING_CONFIG, "STRUCTURED",
            KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "true",
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://mock"
    );
    final KafkaAvroCloudEventSerializer serializer = new KafkaAvroCloudEventSerializer();
    final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> serializer.configure(config, false));
    serializer.close();
    assertTrue(exception.getMessage().contains("not supported"));
  }

  @Test
  void ceMetadataPresent() {
    final SchemaRegistryClient srClient = new MockSchemaRegistryClient();
    final KafkaAvroCloudEventSerializer serializer = new KafkaAvroCloudEventSerializer(srClient);
    final Map<String, String> config = Map.of(
            CloudEventSerializer.ENCODING_CONFIG, "BINARY",
            KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, "true",
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://mock"
    );
    serializer.configure(config, false);

    final CmdEvent cmdEvent = new CmdEvent(new ViewElection("eid", ElectionView.PENDING));
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
    serializer.serialize("topic", headers, ce);

    assertTrue(headers.iterator().hasNext());
    assertEquals("1.0", new String(headers.lastHeader("ce_specversion").value()));
    assertEquals(ce.getId(), new String(headers.lastHeader("ce_id").value()));
    assertEquals(ce.getSource().toString(), new String(headers.lastHeader("ce_source").value()));
    assertEquals(ce.getType(), new String(headers.lastHeader("ce_type").value()));
    assertEquals(ce.getDataContentType(), new String(headers.lastHeader("content-type").value()));
    final Header header = headers.lastHeader(KafkaAvroCloudEventSerializer.DATA_SCHEMA_HEADER);
    assertNotNull(header);
    assertEquals("http://mock/subjects/topic-value/versions/1/schema", new String(header.value()));

    serializer.close();
  }

}