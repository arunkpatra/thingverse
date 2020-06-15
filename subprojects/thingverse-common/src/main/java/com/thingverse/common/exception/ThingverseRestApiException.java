package com.thingverse.common.exception;

public class ThingverseRestApiException extends ThingverseException {

    public ThingverseRestApiException() {
    }

    public ThingverseRestApiException(String message) {
        super(message);
    }

    public ThingverseRestApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThingverseRestApiException(Throwable cause) {
        super(cause);
    }

    public ThingverseRestApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
