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

import akka.actor.typed.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import com.thingverse.backend.AbstractTest;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import com.thingverse.backend.v1.*;
import grpc.health.v1.HealthCheckRequest;
import grpc.health.v1.HealthCheckResponse;
import grpc.health.v1.HealthClient;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.thingverse.grpc.ProtoTransformer.getProtoMapFromJava;

public class ThingverseGrpcClientTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseGrpcClientTest.class);

    @Autowired
    @Qualifier("thingverseApiGrpcClientActorSystem")
    private ActorSystem<Void> actorSystem;

    @Autowired
    private EnhancedThingverseGrpcServiceClient client;

    @Test
    public void checkHealthViaClientTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Running health check test via gRPC.");

        GrpcClientSettings settings =
                GrpcClientSettings.fromConfig("thingverse-service-client", actorSystem.classicSystem());
        Materializer materializer = Materializer.matFromSystem(actorSystem.classicSystem());
        HealthClient healthClient = HealthClient.create(settings, materializer, actorSystem.classicSystem().dispatcher());

        HealthCheckRequest request = HealthCheckRequest.newBuilder().setService("thingverse-backend").build();
        CompletionStage<HealthCheckResponse> stage = healthClient.check(request);
        HealthCheckResponse response = stage.toCompletableFuture().get();
        LOGGER.info("Result = {}", response.getStatus().name());
    }

    @Test
    public void createThingViaGrpcTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Running createThing test via gRPC.");
        LOGGER.info("Type of client is {}", client.getClass().getName());
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Alice");

        CreateThingGrpcRequest request = CreateThingGrpcRequest.newBuilder()
                .putAllAttributes(getProtoMapFromJava(attributes)).build();
        CreateThingGrpcResponse response = client.createThing(request).toCompletableFuture().get();

        Assert.assertTrue(FAILURE_CHAR + "Thing was not created. Response message was: " +
                response.getMessage(), "Thing was created successfully".contentEquals(response.getMessage()));
        LOGGER.info(SUCCESS_CHAR + "Thing created. ThingID: {}, message: {}", response.getThingID(), response.getMessage());

    }

    @Test
    public void getThingViaGrpcTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Running getThing test via gRPC.");

        // Create the thing
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Alice");

        CreateThingGrpcRequest request = CreateThingGrpcRequest.newBuilder()
                .putAllAttributes(getProtoMapFromJava(attributes)).build();
        CreateThingGrpcResponse createThingGrpcResponse = client.createThing(request).toCompletableFuture().get();
        Assert.assertTrue(FAILURE_CHAR + "Thing was not created. Response message was: " +
                        createThingGrpcResponse.getMessage(),
                "Thing was created successfully".contentEquals(createThingGrpcResponse.getMessage()));

        // Now get back
        GetThingGrpcResponse getThingGrpcResponse = client.getThing(GetThingGrpcRequest.newBuilder()
                .setThingID(createThingGrpcResponse.getThingID()).build()).toCompletableFuture().get();
        Assert.assertTrue(FAILURE_CHAR + "Thing was not created. ",
                createThingGrpcResponse.getThingID().contentEquals(getThingGrpcResponse.getThingID()));

        LOGGER.info(SUCCESS_CHAR + "Thing retrieved. ThingID: {}", getThingGrpcResponse.getThingID());
    }

    @Test
    public void updateThingViaGrpcTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Running updateThing test via gRPC.");

        // Create the thing
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Alice");

        CreateThingGrpcRequest request = CreateThingGrpcRequest.newBuilder()
                .putAllAttributes(getProtoMapFromJava(attributes)).build();
        CreateThingGrpcResponse createThingGrpcResponse = client.createThing(request).toCompletableFuture().get();
        Assert.assertTrue(FAILURE_CHAR + "Thing was not created. Response message was: " +
                        createThingGrpcResponse.getMessage(),
                "Thing was created successfully".contentEquals(createThingGrpcResponse.getMessage()));

        // Now Update
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put("temp", 42);
        newAttributes.put("vibration", 1200);
        UpdateThingGrpcRequest updateThingGrpcRequest = UpdateThingGrpcRequest.newBuilder()
                .setThingID(createThingGrpcResponse.getThingID())
                .putAllAttributes(getProtoMapFromJava(newAttributes)).build();

        UpdateThingGrpcResponse updateThingGrpcResponse = client.updateThing(updateThingGrpcRequest).toCompletableFuture().get();
        Assert.assertTrue(FAILURE_CHAR + "Thing was not updated.",
                "Thing updated".contentEquals(updateThingGrpcResponse.getMessage()));

        LOGGER.info(SUCCESS_CHAR + "Thing updated successfully.");
    }

    @Test
    public void clearThingViaGrpcTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Running clearThing test via gRPC.");

        // Create the thing
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Alice");

        CreateThingGrpcRequest request = CreateThingGrpcRequest.newBuilder()
                .putAllAttributes(getProtoMapFromJava(attributes)).build();
        CreateThingGrpcResponse createThingGrpcResponse = client.createThing(request).toCompletableFuture().get();
        Assert.assertTrue(FAILURE_CHAR + "Thing was not created. Response message was: " +
                        createThingGrpcResponse.getMessage(),
                "Thing was created successfully".contentEquals(createThingGrpcResponse.getMessage()));

        // Now clear thing
        ClearThingGrpcRequest clearThingGrpcRequest = ClearThingGrpcRequest.newBuilder()
                .setThingID(createThingGrpcResponse.getThingID()).build();
        ClearThingGrpcResponse clearThingGrpcResponse = client.clearThing(clearThingGrpcRequest)
                .toCompletableFuture().get();
        Assert.assertTrue(FAILURE_CHAR + "Thing was not cleared",
                "Thing was cleared".contentEquals(clearThingGrpcResponse.getMessage()));
        // Get back thing
        GetThingGrpcRequest getThingGrpcRequest = GetThingGrpcRequest.newBuilder()
                .setThingID(createThingGrpcResponse.getThingID()).build();
        GetThingGrpcResponse getThingGrpcResponse = client.getThing(getThingGrpcRequest).toCompletableFuture().get();
        Assert.assertEquals(FAILURE_CHAR + "Thing was not cleared of attributes", 0,
                getThingGrpcResponse.getAttributesMap().size());
    }

    @Test
    public void stopThingViaGrpcTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Running stopThing test via gRPC.");

        // Create the thing
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Alice");

        CreateThingGrpcRequest request = CreateThingGrpcRequest.newBuilder()
                .putAllAttributes(getProtoMapFromJava(attributes)).build();
        CreateThingGrpcResponse createThingGrpcResponse = client.createThing(request).toCompletableFuture().get();
        Assert.assertTrue(FAILURE_CHAR + "Thing was not created. Response message was: " +
                        createThingGrpcResponse.getMessage(),
                "Thing was created successfully".contentEquals(createThingGrpcResponse.getMessage()));

        // Now stop thing
        StopThingGrpcRequest stopThingGrpcRequest = StopThingGrpcRequest.newBuilder()
                .setThingID(createThingGrpcResponse.getThingID()).build();

        StopThingGrpcResponse stopThingGrpcResponse = client.stopThing(stopThingGrpcRequest).toCompletableFuture().get();
        Assert.assertTrue(FAILURE_CHAR + "Thing was not passivated",
                "Thing was passivated".contentEquals(stopThingGrpcResponse.getMessage()));
    }

    @Test
    public void getBackendClusterStatusTest() throws Exception {

        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("b-some-key-1", "value1");
        metadataMap.put("b-some-key-2", "value2");

        GetBackendClusterStatusGrpcResponse response =
                client.getBackendClusterStatus(GetBackendClusterStatusGrpcRequest.newBuilder().build(), metadataMap)
                        .toCompletableFuture().get();


    }

    @Test
    public void getMetricsViaGrpcTest() throws Exception {
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("b-some-key-1", "value1");
        metadataMap.put("b-some-key-2", "value2");

        // Get current metrics
        GetMetricsGrpcResponse currentMetrics =
                client.getMetrics(GetMetricsGrpcRequest.newBuilder().build(), metadataMap)
                        .toCompletableFuture().get();

        // Create two new things
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Alice");
        CreateThingGrpcRequest request = CreateThingGrpcRequest.newBuilder()
                .putAllAttributes(getProtoMapFromJava(attributes)).build();
        CreateThingGrpcResponse createThingGrpcResponse = client.createThing(request).toCompletableFuture().get();

        Assert.assertTrue(FAILURE_CHAR + "Thing was not created. Response message was: " +
                        createThingGrpcResponse.getMessage(),
                "Thing was created successfully".contentEquals(createThingGrpcResponse.getMessage()));
        createThingGrpcResponse = client.createThing(request).toCompletableFuture().get();
        Assert.assertTrue(FAILURE_CHAR + "Thing was not created. Response message was: " +
                        createThingGrpcResponse.getMessage(),
                "Thing was created successfully".contentEquals(createThingGrpcResponse.getMessage()));

        // Get counts again
        GetMetricsGrpcResponse newMetrics = client
                .getMetrics(GetMetricsGrpcRequest.newBuilder().build())
                .toCompletableFuture().get();
        Assert.assertEquals(FAILURE_CHAR + "Did not get correct counts", newMetrics.getCount(),
                currentMetrics.getCount() + 2);
    }

    @Test
    public void streamThingIDsViaGrpcTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Running streaming thingIDs test via gRPC");

        // Create a bunch of things
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Alice");
        CreateThingGrpcRequest request = CreateThingGrpcRequest.newBuilder()
                .putAllAttributes(getProtoMapFromJava(attributes)).build();
        List<String> thingIDList = new ArrayList<>();
        thingIDList.add(client.createThing(request).toCompletableFuture().get().getThingID());
        thingIDList.add(client.createThing(request).toCompletableFuture().get().getThingID());
        thingIDList.add(client.createThing(request).toCompletableFuture().get().getThingID());
        thingIDList.add(client.createThing(request).toCompletableFuture().get().getThingID());
        thingIDList.add(client.createThing(request).toCompletableFuture().get().getThingID());

        // Now stream IDs back
        List<String> retrievedThingIDList = client.streamAllThingIDs(StreamAllThingIDsGrpcRequest.newBuilder()
                .setMaxidstoreturn(50L).build())
                .runWith(Sink.seq(), actorSystem)
                .handleAsync((r, t) -> {
                    if (null == t) {
                        return r;
                    } else {
                        return (List<StreamAllThingIDsGrpcResponse>) new ArrayList<StreamAllThingIDsGrpcResponse>();
                    }
                })
                .toCompletableFuture().get().stream().map(StreamAllThingIDsGrpcResponse::getThingID).collect(Collectors.toList());
        for (String tID : thingIDList) {
            Assert.assertTrue("Thing ID was not retrieved", retrievedThingIDList.contains(tID));
        }
        retrievedThingIDList.forEach(x -> LOGGER.info(SUCCESS_CHAR + "Got thingID {}", x));
    }
}
