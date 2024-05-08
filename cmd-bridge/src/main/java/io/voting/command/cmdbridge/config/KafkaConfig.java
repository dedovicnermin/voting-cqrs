package io.voting.command.cmdbridge.config;

import io.cloudevents.CloudEvent;
import io.voting.command.cmdbridge.sender.CmdSender;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.kafka.clients.serialization.ce.CESerializer;
import io.voting.common.library.models.ElectionVote;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaConfig {

  /**
   * application config (.yml / .properties) does not prefix expression with '$' on purpose
   * If not, spring will replace '${file:/path/to/file.txt:key}' into '/path/to/file.txt:key'
   */
  final BiFunction<String, Object, Object> configProviderReMap = (k, v) -> ((String) v).replace("{", "${");

  @Setter
  private Map<String, String> properties;

  @Setter
  private Map<String, String> producer;

  @Setter
  private Map<String, String> topics;


  public Map<String, Object> producerConfigs() {
    final Map<String, Object> config = new HashMap<>(properties);
    Optional.ofNullable(System.getenv("POD_NAME"))
            .ifPresent(id -> config.put(ConsumerConfig.CLIENT_ID_CONFIG, id));
    config.putAll(producer);
    config.putIfAbsent(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    config.putIfAbsent(ProducerConfig.ACKS_CONFIG, 1);
    if (config.containsKey("config.providers")) {
      config.computeIfPresent("sasl.jaas.config", configProviderReMap);
      config.computeIfPresent("ssl.truststore.password", configProviderReMap);
    }
    return config;
  }

  @Bean
  public Producer<String, CloudEvent> producer() {
    final Producer<String, CloudEvent> kafkaProducer = new KafkaProducer<>(producerConfigs(), new StringSerializer(), new CESerializer());
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      kafkaProducer.flush();
      kafkaProducer.close();
    }));
    return kafkaProducer;
  }

  @Bean
  public EventSender<String, CloudEvent> voteSender(final Producer<String, CloudEvent> producer) {
    return new CmdSender(
            topics.getOrDefault("NEW_VOTE", "election.votes.raw"),
            producer
    );
  }

  @Bean
  public EventSender<String, CloudEvent> electionSender(final Producer<String, CloudEvent> producer) {
    return new CmdSender(
            topics.getOrDefault("NEW_ELECTION", "election.requests.raw"),
            producer
    );
  }


}
