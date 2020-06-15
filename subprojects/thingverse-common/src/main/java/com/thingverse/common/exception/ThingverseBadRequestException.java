package com.thingverse.common.exception;

public class ThingverseBadRequestException extends ThingverseException {

    public ThingverseBadRequestException() {
    }

    public ThingverseBadRequestException(String message) {
        super(message);
    }

    public ThingverseBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThingverseBadRequestException(Throwable cause) {
        super(cause);
    }

    public ThingverseBadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
