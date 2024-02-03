package io.voting.common.library.kafka.clients.sender;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.internals.ErrorLoggingCallback;

import java.io.Closeable;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface EventSender<K, V> extends Closeable {

    Future<RecordMetadata> send(K key, V payload);
    Future<RecordMetadata> send(V payload);

    default RecordMetadata blockingSend(V payload) throws SendException {
        try {
            return send(payload).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new SendException(e.getCause());
        }
    }

    default RecordMetadata blockingSend(K key, V payload) throws SendException {
        try {
            return send(key, payload).get();
        }  catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new SendException(e.getCause());
        }
    }

    default Callback getSenderCb(final String topic, byte[] k, byte[] v) {
        return new ErrorLoggingCallback(
                topic,
                Optional.ofNullable(k).orElse(new byte[]{}),
                v,
                true
        );
    }

}
