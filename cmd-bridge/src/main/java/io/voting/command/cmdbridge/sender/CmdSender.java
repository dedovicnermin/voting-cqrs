package io.voting.command.cmdbridge.sender;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.sender.EventSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.IOException;
import java.util.concurrent.Future;

@Slf4j
public class CmdSender implements EventSender<String, CloudEvent> {

  private final String topic;
  private final Producer<String, CloudEvent> producer;

  public CmdSender(String topic, Producer<String, CloudEvent> producer) {
    this.topic = topic;
    this.producer = producer;
  }

  @Override
  public Future<RecordMetadata> send(String key, CloudEvent payload) {
    final ProducerRecord<String, CloudEvent> event = new ProducerRecord<>(topic, key, payload);
    log.debug("Sending CMD event : {}", event);
    return producer.send(event);
  }

  @Override
  public Future<RecordMetadata> send(CloudEvent payload) {
    return send(null, payload);
  }

  @Override
  public void close() throws IOException {}
}
