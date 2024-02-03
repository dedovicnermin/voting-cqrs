package io.voting.common.library.kafka.clients.receiver;

import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.models.PayloadOrError;
import io.voting.common.library.kafka.models.ReceiveEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.errors.WakeupException;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractEventReceiver<K, V> implements EventReceiver<K, V>, Runnable {

  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final Set<EventListener<K, V>> listeners = new HashSet<>();

  protected final Consumer<K, PayloadOrError<V>> consumer;
  protected final ReceiverRebalanceListener<K, V> rebalanceListener;

  protected AbstractEventReceiver(final Consumer<K, PayloadOrError<V>> consumer) {
    this.consumer = consumer;
    this.rebalanceListener = new ReceiverRebalanceListener<>(consumer);
  }

  @Override
  public void addListener(EventListener<K,V> listener) {
    listeners.add(listener);
  }

  protected final boolean receiverIsRunning() {
    return !closed.get();
  }

  protected final void fire(final ReceiveEvent<K, V> event) {
    for (var listener : listeners) {
      listener.onEvent(event);
    }
  }

  @Override
  public void run() {
    try {
      start();
    } catch (WakeupException e) {
      // do nothing
    } finally {
      consumer.close();
    }
  }

  @Override
  public void close() {
    closed.set(true);
    consumer.wakeup();
  }
}
