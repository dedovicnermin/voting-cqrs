package io.voting.persistence.eventsink.framework;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.clients.receiver.EventReceiver;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;

import java.util.HashSet;
import java.util.Set;

public class TestReceiver implements EventReceiver<String, CloudEvent> {

  final Set<EventListener<String, CloudEvent>> listeners = new HashSet<>();

  @Override
  public void addListener(EventListener<String, CloudEvent> listener) {listeners.add(listener);}

  @Override
  public void start() {}

  @Override
  public void close() {}

  public void trigger(final CloudEvent cloudEvent) {
    final ReceiveEvent<String, CloudEvent> event = new ReceiveEvent<>("test", 0, 0L, 0L, null, new PayloadOrError<>(cloudEvent, null, ""));
    for (EventListener<String, CloudEvent> listener : listeners) {
      listener.onEvent(event);
    }
  }
}
