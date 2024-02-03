package io.voting.common.library.kafka.clients.receiver;

import io.voting.common.library.kafka.clients.listener.EventListener;

import java.io.Closeable;

public interface EventReceiver<K, V> extends Closeable {
    void addListener(EventListener<K, V> listener);
    void start();
    void close();
}
