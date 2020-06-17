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
