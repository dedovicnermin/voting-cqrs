package io.voting.persistence.eventsink.config;

import io.cloudevents.CloudEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.voting.common.library.kafka.clients.receiver.EventReceiver;
import io.voting.common.library.kafka.clients.receiver.ReceiverCloser;
import io.voting.common.library.kafka.clients.serialization.ce.AvroCEPayloadDeserializer;
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
    config.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AvroCEPayloadDeserializer.class);
    config.putIfAbsent(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
    config.putIfAbsent("poll.duration", "1000");
    if (config.containsKey("config.providers")) {
      config.computeIfPresent("sasl.jaas.config", configProviderReMap);
      config.computeIfPresent("ssl.truststore.password", configProviderReMap);
      config.computeIfPresent("schema.registry.ssl.truststore.password", configProviderReMap);
      config.computeIfPresent("schema.registry.basic.auth.user.info", configProviderReMap);
    }
    return config;
  }

  @Bean
  public EventReceiver<String, CloudEvent> eventReceiver() {
    final Map<String, Object> configs = consumerConfigs();
    final KafkaConsumer<String, PayloadOrError<CloudEvent>> kafkaConsumer = new KafkaConsumer<>(configs);
    final CloudEventReceiver receiver = new CloudEventReceiver(kafkaConsumer, consumerTopics, Duration.ofMillis(Long.parseLong((String) configs.get("poll.duration"))));
    Runtime.getRuntime().addShutdownHook(new Thread(new ReceiverCloser<>(receiver)));
    new Thread(receiver).start();
    return receiver;
  }

}
