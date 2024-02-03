package io.voting.streams.voteintegrity.topology;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class TestKafkaContext {

  public static final KafkaContainer kafkaContainer;

  static {
    kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withEnv("KAFKA_NUM_PARTITIONS", "1");
    kafkaContainer.start();
  }
}
