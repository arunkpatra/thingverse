package com.thingverse.common.exception;

/**
 * Base exception for this application
 */
public class ThingverseException extends Exception {
    public ThingverseException() {
    }

    public ThingverseException(String message) {
        super(message);
    }

    public ThingverseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThingverseException(Throwable cause) {
        super(cause);
    }

    public ThingverseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
