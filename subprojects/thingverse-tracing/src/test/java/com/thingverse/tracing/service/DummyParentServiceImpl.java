package com.thingverse.tracing.service;

import akka.grpc.javadsl.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thingverse.tracing.annotation.Traced;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class DummyParentServiceImpl implements DummyParentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyParentServiceImpl.class);
    private final DummyChildService dummyChildService;

    public DummyParentServiceImpl(DummyChildService dummyChildService) {
        this.dummyChildService = dummyChildService;
    }

    @Override
    @Traced(operationName = "some-parent-method")
    public String someParentMethod() {
        String parentMessage = "Hello World from Parent";
        String childMessage = dummyChildService.someChildMethod();
        LOGGER.info("Returning: {} -> {}", parentMessage, childMessage);
        return String.format("%s -> %s", parentMessage, childMessage);
    }

    @Override
    @Traced(operationName = "some-parent-method-future")
    public CompletionStage<String> someParentMethodFuture() {
        return CompletableFuture.supplyAsync(() -> "Hello World from future parent");
    }

    @Override
    @Traced(operationName = "some-parent-method-future-exception")
    public boolean someParentMethodFutureException() {
        return CompletableFuture.supplyAsync(() -> "Hello exception")
                .completeExceptionally(new ExecutionException(new StatusRuntimeException(Status.CANCELLED)));
    }

    @Override
    @Traced(operationName = "some-parent-method-with-metadata", spanType = Traced.SpanType.PROPAGATED)
    public String someParentMethodWithMetadata(Metadata metadata) {
        String parentMessage = "Hello World from Parent";
        String childMessage = dummyChildService.someChildMethod();
        LOGGER.info("Returning: {} -> {}", parentMessage, childMessage);
        return String.format("%s -> %s", parentMessage, childMessage);
    }

    @Override
    @Traced(operationName = "some-parent-method-with-metadata2")
    public String someParentMethodWithMetadata2(Metadata metadata) throws Exception {
        String parentMessage = "Hello World from Parent";
        try {
            String childMessage = dummyChildService.someChildMethodWithMetadataWithException(metadata);
            LOGGER.info("Returning: {} -> {}", parentMessage, childMessage);
            return String.format("%s -> %s", parentMessage, childMessage);
        } catch (Exception e) {
            throw e;
        }

    }

    @Override
    @Traced(operationName = "some-parent-method-with-metadata-exception", spanType = Traced.SpanType.NEW)
    public String someParentMethodWithMetadataWithException(Metadata metadata) throws ExecutionException {
        StatusRuntimeException sre = new StatusRuntimeException(Status.NOT_FOUND);
        throw new ExecutionException("Dummy Exception", sre);
    }
}
