package io.voting.common.library.kafka.clients.receiver;


import lombok.extern.slf4j.Slf4j;

/**
 * Responsible for closing EventReceiver
 * @param <K> key type
 * @param <V> value type
 */
@Slf4j
public final class ReceiverCloser<K, V> implements Runnable {

    private final EventReceiver<K, V> receiver;

    public ReceiverCloser(final EventReceiver<K, V> receiver) {
        this.receiver = receiver;
    }

    @Override
    public void run() {
        try {
            log.info("Closing receiver : {}", receiver);
            receiver.close();

            Thread.sleep(5000);
        } catch (Exception e) {
            log.error("Issues with closing consumer: {}", e.getMessage());
        }
    }
}
