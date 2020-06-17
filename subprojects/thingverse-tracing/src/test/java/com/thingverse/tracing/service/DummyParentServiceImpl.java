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
