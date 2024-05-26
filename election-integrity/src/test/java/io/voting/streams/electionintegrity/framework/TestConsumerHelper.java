package io.voting.streams.electionintegrity.framework;

import io.cloudevents.CloudEvent;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.voting.common.library.kafka.clients.serialization.ce.AvroCEPayloadDeserializer;
import io.voting.common.library.kafka.clients.serialization.ce.CEPayloadDeserializer;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;
import lombok.Getter;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.containers.KafkaContainer;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TestConsumerHelper {

  public static final String INPUT_TOPIC = "election.requests.raw";
  public static final String OUTPUT_TOPIC_ELECTION = "election.requests";
  public static final String OUTPUT_TOPIC_VOTES = "election.votes";

  static final NewTopic IN = new NewTopic(INPUT_TOPIC, 1, (short) 1);
  static final NewTopic OUT = new NewTopic(OUTPUT_TOPIC_ELECTION, 1, (short) 1);
  static final NewTopic OUT_VOTES = new NewTopic(OUTPUT_TOPIC_VOTES, 1, (short) 1);


  @Getter
  private final BlockingQueue<ReceiveEvent<String, CloudEvent>> events = new LinkedBlockingQueue<>();

  public TestConsumerHelper(final KafkaContainer kafkaContainer, final String consumeTopic) {
    final Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
            kafkaContainer.getBootstrapServers(),
            "eiTest",
            "true"
    );
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AvroCEPayloadDeserializer.class.getName());
    consumerProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,TestKafkaContext.schemaRegistryUrl());
    consumerProps.put("specific.avro.reader", true);
    createTargetTopics(kafkaContainer);

    final KafkaMessageListenerContainer<String, PayloadOrError<CloudEvent>> listenerContainer = getListenerContainer(consumerProps, consumeTopic);
    listenerContainer.setupMessageListener(getMessageListener());
    listenerContainer.start();
    ContainerTestUtils.waitForAssignment(listenerContainer, 1);
    Runtime.getRuntime().addShutdownHook(new Thread(listenerContainer::stop));
  }

  public void clearQueues() {
    events.clear();
  }

  @NotNull
  private static KafkaMessageListenerContainer<String, PayloadOrError<CloudEvent>> getListenerContainer(Map<String, Object> consumerProps, String consumeTopic) {
    final DefaultKafkaConsumerFactory<String, PayloadOrError<CloudEvent>> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
    final ContainerProperties containerProperties = new ContainerProperties(consumeTopic);
    return new KafkaMessageListenerContainer<>(cf, containerProperties);
  }

  @NotNull
  private MessageListener<String, PayloadOrError<CloudEvent>> getMessageListener() {
    return data -> {
      final ReceiveEvent<String, CloudEvent> re = new ReceiveEvent<>(
              data.topic(), data.partition(), data.offset(), data.timestamp(), data.key(), data.value());
      System.out.println("TestConsumer received : " + re);
      events.add(re);
    };
  }


  private static void createTargetTopics(KafkaContainer kafkaContainer) {
    final KafkaAdmin kafkaAdmin = new KafkaAdmin(Map.of("bootstrap.servers", kafkaContainer.getBootstrapServers()));
    kafkaAdmin.createOrModifyTopics(IN, OUT, OUT_VOTES);
    final Collection<TopicDescription> values = kafkaAdmin.describeTopics(
            IN.name(), OUT.name(), OUT_VOTES.name()
    ).values();
    values.forEach(td -> System.out.println("TOPIC : " + td));
    kafkaAdmin.setCloseTimeout(1000);
  }


}
