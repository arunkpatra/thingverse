package com.thingverse.tracing.service;

import akka.grpc.javadsl.Metadata;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public interface DummyParentService {
    String someParentMethod();
    String someParentMethodWithMetadata(Metadata metadata);
    String someParentMethodWithMetadataWithException(Metadata metadata) throws ExecutionException;
    String someParentMethodWithMetadata2(Metadata metadata) throws Exception;
    CompletionStage<String> someParentMethodFuture();
    boolean someParentMethodFutureException();
}
