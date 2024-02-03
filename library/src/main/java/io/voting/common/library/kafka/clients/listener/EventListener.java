package io.voting.common.library.kafka.clients.listener;

import io.voting.common.library.kafka.models.ReceiveEvent;

@FunctionalInterface
public interface EventListener<K, V> {
    void onEvent(ReceiveEvent<K, V> event);
}
