package io.voting.streams.voteintegrity;

import io.voting.streams.voteintegrity.config.Constants;
import io.voting.streams.voteintegrity.topology.VoteIntegrityTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import io.voting.common.library.kafka.utils.StreamUtils;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class VoteIntegrityApplication {

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
      log.info(" --- vote-integrity application started --- ");
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

    if (Objects.isNull(properties.getProperty(Constants.INPUT_TOPIC_CONFIG)) ||
            Objects.isNull(properties.getProperty(Constants.OUTPUT_TOPIC_CONFIG))) {
      throw new RuntimeException("Missing input/output topic configuration");
    }

    final Topology topology = VoteIntegrityTopology
            .buildTopology(new StreamsBuilder(), properties);
    log.debug(" ::: App topology :::\n{}", topology.describe());
    return new KafkaStreams(topology, properties);
  }


}
