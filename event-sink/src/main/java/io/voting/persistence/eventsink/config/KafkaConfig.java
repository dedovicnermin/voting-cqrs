package io.voting.persistence.eventsink.config;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.receiver.EventReceiver;
import io.voting.common.library.kafka.clients.receiver.ReceiverCloser;
import io.voting.common.library.kafka.clients.serialization.ce.CEPayloadDeserializer;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.persistence.eventsink.receiver.CloudEventReceiver;
import lombok.Getter;
import lombok.Setter;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaConfig {

  @Setter
  private Map<String, String> properties;

  @Setter
  private Map<String, String> consumer;

  @Getter
  @Setter
  private List<String> consumerTopics;


  public Map<String, Object> consumerConfigs() {
    final Map<String, Object> config = new HashMap<>(properties);
    Optional.ofNullable(System.getenv("POD_NAME"))
            .ifPresent(id -> config.put(ConsumerConfig.CLIENT_ID_CONFIG, id));
    config.putAll(consumer);
    config.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    config.putIfAbsent("poll.duration", "1000");
    return config;
  }

  @Bean
  public EventReceiver<String, CloudEvent> eventReceiver() {
    final Map<String, Object> configs = consumerConfigs();
    final KafkaConsumer<String, PayloadOrError<CloudEvent>> kafkaConsumer = new KafkaConsumer<>(configs, new StringDeserializer(), new CEPayloadDeserializer());
    final CloudEventReceiver receiver = new CloudEventReceiver(kafkaConsumer, consumerTopics, Duration.ofMillis(Long.parseLong((String) configs.get("poll.duration"))));
    Runtime.getRuntime().addShutdownHook(new Thread(new ReceiverCloser<>(receiver)));
    new Thread(receiver).start();
    return receiver;
  }

}
