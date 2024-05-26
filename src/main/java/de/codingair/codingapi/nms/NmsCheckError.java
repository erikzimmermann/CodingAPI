package de.codingair.codingapi.nms;

public class NmsCheckError extends RuntimeException {
    public NmsCheckError() {
    }

    public NmsCheckError(String message) {
        super(message);
    }

    public NmsCheckError(String message, Throwable cause) {
        super(message, cause);
    }

    public NmsCheckError(Throwable cause) {
        super(cause);
    }

    public NmsCheckError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
