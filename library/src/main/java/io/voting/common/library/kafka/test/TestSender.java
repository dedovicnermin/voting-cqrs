package io.voting.common.library.kafka.test;

import io.voting.common.library.kafka.clients.sender.EventSender;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.Future;

public class TestSender<K, V> implements EventSender<K, V> {

  private final String topic;
  private final Producer<K, V> producer;

  public TestSender(String topic, Producer<K, V> producer) {
    this.topic = topic;
    this.producer = producer;
  }

  @Override
  public Future<RecordMetadata> send(K key, V payload) {
    return producer.send(new ProducerRecord<>(topic, key, payload));
  }

  @Override
  public Future<RecordMetadata> send(V payload) {
    return producer.send(new ProducerRecord<>(topic, payload));
  }

  @Override
  public void close() {
    producer.close();
  }
}
