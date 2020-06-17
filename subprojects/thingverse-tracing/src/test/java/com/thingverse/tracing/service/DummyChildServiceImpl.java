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
