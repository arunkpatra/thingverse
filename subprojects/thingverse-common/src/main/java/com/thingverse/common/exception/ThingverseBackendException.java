package com.thingverse.common.exception;

/**
 * Base exception for this application
 */
public class ThingverseBackendException extends ThingverseException {
    private static final String defaultMessage = "Backend Unavailable";

    public ThingverseBackendException() {
        super(defaultMessage);
    }

    public ThingverseBackendException(String message) {
        super(message);
    }

    public ThingverseBackendException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThingverseBackendException(Throwable cause) {
        super(defaultMessage, cause);
    }

    public ThingverseBackendException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
