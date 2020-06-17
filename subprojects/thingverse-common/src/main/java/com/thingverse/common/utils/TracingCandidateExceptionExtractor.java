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

package com.thingverse.common.utils;

import com.thingverse.common.exception.ThingverseBackendException;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.ExecutionException;

public class TracingCandidateExceptionExtractor {
    public static Throwable getCandidate(Throwable t) {
        Throwable candidate = null;
        if (t instanceof java.util.concurrent.ExecutionException) {
            if ((null != t.getCause()) && (t.getCause() instanceof StatusRuntimeException)) {
                return t.getCause();
            }
        } else {
            if (t instanceof ThingverseBackendException) {
                if ((null != t.getCause()) && (t.getCause() instanceof ExecutionException)) {
                    Throwable t1 = t.getCause();
                    if ((null != t1.getCause()) && (t1.getCause() instanceof StatusRuntimeException)) {
                        return t1.getCause();
                    }
                }
            }
        }
        return candidate;
    }
}
