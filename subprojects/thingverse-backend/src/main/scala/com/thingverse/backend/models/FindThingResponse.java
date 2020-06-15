package com.thingverse.backend.models;

import java.util.Optional;

public class FindThingResponse {
    private final boolean found;
    private final Optional<Throwable> throwable;

    public FindThingResponse(boolean found, Optional<Throwable> throwable) {
        this.found = found;
        this.throwable = throwable;
    }

    public boolean isFound() {
        return found;
    }

    public Optional<Throwable> getThrowable() {
        return throwable;
    }
}
