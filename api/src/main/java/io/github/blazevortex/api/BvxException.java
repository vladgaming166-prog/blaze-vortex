package io.github.blazevortex.api;

public class BvxException extends RuntimeException {

    public BvxException(String message) {
        super(message);
    }

    public BvxException(String message, Throwable cause) {
        super(message, cause);
    }
}
