package io.voting.command.cmdbridge.controller.framework;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.voting.events.cmd.CreateElection;
import io.voting.events.cmd.RegisterVote;
import io.voting.events.cmd.ViewElection;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Testcontainers
public abstract class TestKafkaContext {

  private static final String CMD_EVENT_SCHEMA = "{\"type\":\"record\",\"name\":\"CmdEvent\",\"namespace\":\"io.voting.events.cmd\",\"doc\":\"Events emitted from the cmd-bridge component\",\"fields\":[{\"name\":\"cmd\",\"type\":[\"io.voting.events.cmd.CreateElection\",\"io.voting.events.cmd.ViewElection\",\"io.voting.events.cmd.RegisterVote\"]}]}";

  public static final KafkaContainer kafkaContainer;
  public static final SchemaRegistryContainer srContainer;

  static {
    kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withKraft()
            .withListener(() -> "kafka:19092")
            .withNetwork(Network.SHARED)
            .withEnv("KAFKA_NUM_PARTITIONS", "1");
    kafkaContainer.start();
    srContainer = new SchemaRegistryContainer().withKafka(kafkaContainer);
    srContainer.start();
  }

  public static String schemaRegistryUrl() {
    return "http://" + srContainer.getHost() + ":" + srContainer.getFirstMappedPort();
  }

  public static void registerSchemas() {
    try (final SchemaRegistryClient client = new CachedSchemaRegistryClient(schemaRegistryUrl(), 10)) {
      registerEmbeddedOneOfs(client);
      registerCmdEventSchema(client);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
  }

  private static void registerCmdEventSchema(final SchemaRegistryClient client) {
    try {
      final List<SchemaReference> references = Arrays.asList(
              new SchemaReference(CreateElection.class.getName(), "create-election-cmd", 1),
              new SchemaReference(ViewElection.class.getName(), "view-election-cmd", 1),
              new SchemaReference(RegisterVote.class.getName(), "register-vote-cmd", 1)
      );
      final Map<String, String> resolvedReferences = Map.of(
              CreateElection.class.getName(), CreateElection.getClassSchema().toString(),
              ViewElection.class.getName(), ViewElection.getClassSchema().toString(),
              RegisterVote.class.getName(), RegisterVote.getClassSchema().toString()
      );
      final AvroSchema cmdEventSchema = new AvroSchema(
              CMD_EVENT_SCHEMA,
              references,
              resolvedReferences,
              -1,
              false
      );
      int cmdEventId = client.register("test.election.commands-value", cmdEventSchema, true);
      System.out.println("Registered cmd-event: " + cmdEventId);

    } catch (RestClientException e) {
      System.err.println("RestClientException during registration of cmd-event schema : " + e.getMessage());
      throw new RuntimeException(e);
    } catch (IOException e) {
      System.err.println("IOException during registration of cmd-event schema : " + e.getMessage());
      throw new RuntimeException(e);
    }

  }

  private static void registerEmbeddedOneOfs(final SchemaRegistryClient client) {
    try {

      int viewElectionId = client.register("view-election-cmd", new AvroSchema(ViewElection.getClassSchema()));
      System.out.println("Registered view election: " + viewElectionId);

      int registerVoteId = client.register("register-vote-cmd", new AvroSchema(RegisterVote.getClassSchema()));
      System.out.println("Registered register vote: " + registerVoteId);

      int createElectionId = client.register("create-election-cmd", new AvroSchema(CreateElection.getClassSchema()));
      System.out.println("Registered register vote: " + createElectionId);

    } catch (RestClientException e) {
      System.err.println("RestClientException during registration of embedded 'one-of' schemas : " + e.getMessage());
      throw new RuntimeException(e);
    } catch (IOException e) {
      System.err.println("IO error during registration of embedded 'one-of' schemas : " + e.getMessage());
      throw new RuntimeException(e);
    }

  }
}
