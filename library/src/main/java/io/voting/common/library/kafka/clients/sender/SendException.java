package io.voting.common.library.kafka.clients.sender;

import java.io.Serial;

public final class SendException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public SendException(final String msg) {
        super(msg);
    }

    public SendException(final Throwable cause) {
        super(cause);
    }

    public SendException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public SendException() {
        super();
    }
}
