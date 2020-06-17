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

package com.thingverse.backend.grpc;

import akka.grpc.internal.GrpcMetadataImpl;
import akka.grpc.internal.JavaMetadataImpl;
import akka.grpc.javadsl.Metadata;
import com.thingverse.backend.AbstractTest;
import com.thingverse.backend.actors.RemoteThing;
import com.thingverse.backend.services.ActorService;
import com.thingverse.backend.v1.*;
import grpc.health.v1.GrpcHealthServiceImpl;
import grpc.health.v1.Health;
import grpc.health.v1.HealthCheckRequest;
import grpc.health.v1.HealthCheckResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

public class ThingverseGrpcServiceTest extends AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseGrpcServiceTest.class);
    private final ActorService mockedActorService = Mockito.mock(ActorService.class);
    @Autowired
    private ActorService actorService;

    @Test
    public void healthEndpointViaServiceTest() throws Exception {
        HealthCheckResponse healthCheckResponse = getGrpcHealthService()
                .check(HealthCheckRequest.newBuilder().setService("thingverse-backend").build())
                .toCompletableFuture().get();

        Assert.assertEquals(FAILURE_CHAR + "Status did not match",
                HealthCheckResponse.ServingStatus.SERVING,
                healthCheckResponse.getStatus());
    }

    @Test(expected = java.util.concurrent.ExecutionException.class)
    public void createThingExceptionTest() throws Exception {
        CompletableFuture<CreateThingGrpcResponse> mockResponseException =
                CompletableFuture.supplyAsync(() -> CreateThingGrpcResponse.newBuilder().setErrormessage("Junk Exception").build());
        mockResponseException.completeExceptionally(new RuntimeException("Junk Exception"));
        doReturn(mockResponseException).when(mockedActorService)
                .createThing(any(CreateThingGrpcRequest.class), any(Metadata.class));

        mockedActorService.createThing(CreateThingGrpcRequest.newBuilder()
                .putAllAttributes(new HashMap<>()).build(), getTestMetadata()).toCompletableFuture().get();
    }

    @Test
    public void createThingRejectionTest() throws Exception {
        CompletionStage<CreateThingGrpcResponse> mockResponseRejected =
                CompletableFuture.supplyAsync(() -> CreateThingGrpcResponse.newBuilder().setMessage("Go to hell").build());
        doReturn(mockResponseRejected).when(mockedActorService).createThing(any(CreateThingGrpcRequest.class),
                any(Metadata.class));

        CreateThingGrpcResponse response =
                mockedActorService.createThing(CreateThingGrpcRequest.newBuilder()
                        .putAllAttributes(new HashMap<>()).build(), getTestMetadata())
                        .toCompletableFuture().get();

        Assert.assertEquals(FAILURE_CHAR + "Error message did not match", "Go to hell",
                response.getMessage());
    }

    @Test(expected = java.util.concurrent.ExecutionException.class)
    public void getThingExceptionTest() throws Exception {
        CompletableFuture<GetThingGrpcResponse> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException("Junk Exception"));
        doReturn(mockResponseException).when(mockedActorService).getThing(any(GetThingGrpcRequest.class),
                any(Metadata.class));

        mockedActorService.getThing(GetThingGrpcRequest.newBuilder().setThingID("Junk-thing-id").build(),
                getTestMetadata()).toCompletableFuture().get();
    }

    @Test(expected = java.util.concurrent.ExecutionException.class)
    public void stopThingExceptionTest() throws Exception {
        CompletableFuture<StopThingGrpcResponse> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException("Junk Exception"));
        doReturn(mockResponseException).when(mockedActorService).stopThing(any(StopThingGrpcRequest.class),
                any(Metadata.class));

        mockedActorService.stopThing(StopThingGrpcRequest.newBuilder().setThingID("Junk-thing-id").build(),
                getTestMetadata()).toCompletableFuture().get();
    }

    @Test(expected = java.util.concurrent.ExecutionException.class)
    public void clearThingExceptionTest() throws Exception {
        CompletableFuture<RemoteThing.ThingClearedSummary> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException("Junk Exception"));
        doReturn(mockResponseException).when(mockedActorService).clearThing(any(ClearThingGrpcRequest.class),
                any(Metadata.class));

        mockedActorService.clearThing(ClearThingGrpcRequest.newBuilder().setThingID("Junk-thing-id").build(),
                getTestMetadata()).toCompletableFuture().get();
    }

    @Test
    public void updateThingRejectionTest() throws Exception {
        ActorService originalService = (ActorService) getThingverseGrpcService();
        // Create the thing
        CreateThingGrpcResponse createThingGrpcResponse =
                originalService.createThing(CreateThingGrpcRequest.newBuilder()
                        .putAllAttributes(new HashMap<>()).build()).toCompletableFuture().get();
        String thingID = createThingGrpcResponse.getThingID();

        CompletionStage<UpdateThingGrpcResponse> mockResponseRejected = CompletableFuture.supplyAsync(() ->
                UpdateThingGrpcResponse.newBuilder().setMessage("Go to hell").build());
        doReturn(mockResponseRejected).when(mockedActorService).updateThing(any(UpdateThingGrpcRequest.class),
                any(Metadata.class));

        UpdateThingGrpcResponse response =
                mockedActorService.updateThing(UpdateThingGrpcRequest.newBuilder().setThingID(thingID)
                        .putAllAttributes(new HashMap<>()).build(), getTestMetadata()).toCompletableFuture().get();
        Assert.assertEquals(FAILURE_CHAR + "Error message did not match", "Go to hell",
                response.getMessage());
    }

    @Test(expected = java.util.concurrent.ExecutionException.class)
    public void updateThingExceptionTest() throws Exception {
        CompletableFuture<UpdateThingGrpcResponse> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException("Junk Exception"));
        doReturn(mockResponseException).when(mockedActorService).updateThing(any(UpdateThingGrpcRequest.class),
                any(Metadata.class));

        mockedActorService.updateThing(
                UpdateThingGrpcRequest.newBuilder().setThingID("Junk-thing-id").build(), getTestMetadata())
                .toCompletableFuture().get();
    }

    @Test(expected = java.util.concurrent.ExecutionException.class)
    public void getMetricsExceptionTest() throws Exception {
        CompletableFuture<GetMetricsGrpcResponse> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException("Junk Exception"));
        doReturn(mockResponseException).when(mockedActorService).getMetrics(any(GetMetricsGrpcRequest.class),
                any(Metadata.class));

        mockedActorService.getMetrics(GetMetricsGrpcRequest.newBuilder().build(), getTestMetadata())
                .toCompletableFuture().get();
    }

    private Metadata getTestMetadata() {
        io.grpc.Metadata m = new io.grpc.Metadata();
        m.put(io.grpc.Metadata.Key.of("x-b3-spanid", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "c9f621954448e21e");
        m.put(io.grpc.Metadata.Key.of("x-b3-parentspanid", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "6aa95a61699d289e");
        m.put(io.grpc.Metadata.Key.of("x-b3-sampled", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "1");
        m.put(io.grpc.Metadata.Key.of("x-b3-traceid", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "6aa95a61699d289e");

        return new JavaMetadataImpl(new GrpcMetadataImpl(m));
    }

    private ThingverseGrpcService getThingverseGrpcService() {
        return actorService;
    }

    private Health getGrpcHealthService() {
        return new GrpcHealthServiceImpl();
    }
}
