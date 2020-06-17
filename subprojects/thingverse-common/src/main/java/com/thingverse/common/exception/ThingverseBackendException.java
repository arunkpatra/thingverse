/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
