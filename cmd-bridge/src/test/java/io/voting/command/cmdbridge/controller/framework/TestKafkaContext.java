package io.voting.command.cmdbridge.controller.framework;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class TestKafkaContext {

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
}
