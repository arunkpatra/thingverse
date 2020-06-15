package com.thingverse.tracing.service;

import akka.grpc.javadsl.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thingverse.tracing.annotation.Traced;

import java.util.concurrent.ExecutionException;

public class DummyChildServiceImpl implements DummyChildService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DummyChildServiceImpl.class);

    @Override
    @Traced(operationName = "some-child-method", spanType = Traced.SpanType.EXISTING)
    public String someChildMethod() {
        String childMessage = "Hello World from Child";
        LOGGER.info("Returning: {}", childMessage);
        return childMessage;
    }

    @Override
    @Traced(operationName = "some-child-method-exception", spanType = Traced.SpanType.EXISTING)
    public String someChildMethodWithMetadataWithException(Metadata metadata) throws ExecutionException {
        StatusRuntimeException sre = new StatusRuntimeException(Status.NOT_FOUND);
        throw new ExecutionException("Dummy Exception", sre);
    }
}
