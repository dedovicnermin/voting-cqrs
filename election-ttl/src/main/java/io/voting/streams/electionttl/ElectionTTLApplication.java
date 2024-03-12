package io.voting.streams.electionttl;

import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.streams.electionttl.topology.TTLTopology;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ElectionTTLApplication {

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
      log.info(" --- election-ttl application started --- ");
      countDownLatch.await();

    } catch (InterruptedException e) {
      log.error("Interrupt exception occurred : {}", e.getMessage(), e);
      Thread.currentThread().interrupt();
      log.error("Shutting down application after interrupting thread...");
      System.exit(1);
    } catch (Exception e) {
      log.error("Unexpected exception occurred : {}", e.getMessage(), e);
      log.error("Shutting down application...");
      System.exit(1);
    }

    log.info("Shutting down application...");
    System.exit(0);
  }

  private static KafkaStreams buildStreams(final Properties properties) {
    properties.putIfAbsent(ElectionTTLConfig.ELECTION_REQUESTS_TOPIC, "election.requests");
    properties.putIfAbsent(ElectionTTLConfig.ELECTION_VOTES_TOPIC, "election.votes");
    properties.putIfAbsent(ElectionTTLConfig.VOTE_INTEGRITY_CHANGELOG_TOPIC, "vote.integrity.001-votes.integrity.aggregate-changelog");
    properties.putIfAbsent(ElectionTTLConfig.ELECTION_TTL, "P1D"); // default 1D TTL
    properties.putIfAbsent("client.id", "ttl-streams");

    final Topology topology = TTLTopology.buildTopology(new StreamsBuilder(), properties);
    return new KafkaStreams(topology, properties);
  }
}