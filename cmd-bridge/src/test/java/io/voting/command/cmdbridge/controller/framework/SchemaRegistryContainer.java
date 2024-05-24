package io.voting.command.cmdbridge.controller.framework;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

public class SchemaRegistryContainer extends GenericContainer<SchemaRegistryContainer> {

  public static final String SCHEMA_REGISTRY_IMAGE = "confluentinc/cp-schema-registry";
  public static final int SCHEMA_REGISTRY_PORT = 8081;

  public SchemaRegistryContainer() {
    this("7.6.1");
  }

  public SchemaRegistryContainer(String version) {
    super(SCHEMA_REGISTRY_IMAGE + ":" + version);

    waitingFor(Wait.forHttp("/subjects").forStatusCode(200));
    withExposedPorts(SCHEMA_REGISTRY_PORT);
  }

  public SchemaRegistryContainer withKafka(KafkaContainer kafka) {
//    return withKafka(kafka.getNetwork(), kafka.getBootstrapServers());
    return withKafkaC(kafka);
  }

  public SchemaRegistryContainer withKafkaC(KafkaContainer kafka) {
    withNetwork(kafka.getNetwork());
    withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry");
    withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:"+SCHEMA_REGISTRY_PORT);
    withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "kafka:19092");
    return self();
  }
}
