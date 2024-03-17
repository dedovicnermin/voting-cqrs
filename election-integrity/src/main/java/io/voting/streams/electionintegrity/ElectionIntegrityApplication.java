package io.voting.streams.electionintegrity;

import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.streams.electionintegrity.topology.ElectionIntegrityTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ElectionIntegrityApplication {

  public static void main(String[] args) {
    if (args.length < 1) throw new IllegalArgumentException("Pass path to application.properties");

    final CountDownLatch countDownLatch = new CountDownLatch(1);
    final Properties properties = new Properties();

    try {
      StreamUtils.loadConfigFromFile(args[0], properties);
      Optional.ofNullable(System.getenv().get("POD_NAME"))
              .ifPresent(podId -> properties.put("client.id", podId));

      final KafkaStreams kafkaStreams = buildStreams(properties);
      Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
        @Override
        public void run() {
          log.info("Gracefully closing streams...");
          kafkaStreams.close();
          countDownLatch.countDown();
        }
      });

      kafkaStreams.start();
      log.info(" --- election-integrity application started --- ");
      countDownLatch.await();

    } catch (Exception e) {
      log.error("Unexpected exception occurred : {}", e.getMessage(), e);
      log.error("Shutting down application...");
      System.exit(1);
    }

    log.info("Shutting down application...");
    System.exit(0);
  }

  private static KafkaStreams buildStreams(final Properties properties) {
    if (Objects.isNull(properties.getProperty("input.topic")) || Objects.isNull(properties.getProperty("output.topic"))) {
      throw new RuntimeException("Missing input/output topic configuration");
    }

    properties.putIfAbsent("election.ttl", "P1D"); // default 1D TTL

    final Topology topology = ElectionIntegrityTopology.buildTopology(new StreamsBuilder(), properties);
    return new KafkaStreams(topology, properties);
  }
}