package com.thingverse.tracing.service;

import akka.grpc.javadsl.Metadata;

import java.util.concurrent.ExecutionException;

public interface DummyChildService {

    String someChildMethod();
    String someChildMethodWithMetadataWithException(Metadata metadata) throws ExecutionException;
}
