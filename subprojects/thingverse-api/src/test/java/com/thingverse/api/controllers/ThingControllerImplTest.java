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

package com.thingverse.api.controllers;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.thingverse.api.AbstractTest;
import com.thingverse.api.models.*;
import com.thingverse.backend.v1.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.thingverse.api.AbstractTest.MethodCall.*;
import static com.thingverse.grpc.ProtoTransformer.getProtoMapFromJava;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * For matchers docs see:
 * https://www.petrikainulainen.net/programming/spring-framework/integration-testing-of-spring-mvc-applications-write-clean-assertions-with-jsonpath/
 */
@WithMockUser(username = "dummy_user")
public class ThingControllerImplTest extends AbstractTest {

    protected static final String INVALID_ATTRIBUTES = "Invalid attributes passed.";
    protected static final String BACKEND_EXCEPTION = "Backend Unavailable";
    private static final Logger LOGGER = LoggerFactory.getLogger(ThingControllerImplTest.class);
    private final String thingName = "Hello Thing";
    private final String thingNameKey = "name";
    private final String testThingId = "test-thing-id";
    private final Map<String, Object> testInputAttributes = new HashMap<String, Object>() {{
        put(thingNameKey, thingName);
    }};
    private final Map<String, Object> testUpdateAttributes = new HashMap<String, Object>() {{
        put("temp", 42D);
    }};

    @Test
    public void parseCreateThingRequest() throws Exception {
        String data = "{ \"attributes\" : { \"temp\" : 42 , \"vibration\" : 1200 } }";
        CreateThingRequest request = objectMapper.readValue(data, CreateThingRequest.class);
        assertTrue(request.getAttributes().containsKey("temp"), FAILURE_CHAR + "Did not contain temp attribute");
        assertEquals(42, (int) request.getAttributes().get("temp"), FAILURE_CHAR + "Did not get expected value");

        String dataString = objectMapper.writer().forType(CreateThingRequest.class).writeValueAsString(request);
        assertEquals("{\"attributes\":{\"temp\":42,\"vibration\":1200}}",
                dataString, FAILURE_CHAR + "Data did not match");
    }

    @Test
    public void parseUpdateThingRequest() throws Exception {
        String data = "{ \"attributes\" : { \"temp\" : 42 , \"vibration\" : 1200 } }";
        UpdateThingRequest request = objectMapper.readValue(data, UpdateThingRequest.class);
        assertTrue(request.getAttributes().containsKey("temp"), FAILURE_CHAR + "Did not contain temp attribute");
        assertEquals(42, (int) request.getAttributes().get("temp"), FAILURE_CHAR + "Did not get expected value");

        String dataString = objectMapper.writer().forType(UpdateThingRequest.class).writeValueAsString(request);
        assertEquals("{\"attributes\":{\"temp\":42,\"vibration\":1200}}",
                dataString, FAILURE_CHAR + "Data did not match");
    }


    @Test
    public void createThingTest() throws Exception {
        CompletionStage<CreateThingGrpcResponse> mockResponse = CompletableFuture.supplyAsync(() ->
                CreateThingGrpcResponse.newBuilder().setThingID(testThingId).setMessage("Thing was created").build());
        CreateThingResponse createThingResponse = authenticatedMockHttpExchange(
                CREATE_THING,
                mockResponse,
                post("/api/thing"),
                status().isCreated(),
                Optional.of(new CreateThingRequest(testInputAttributes)),
                Optional.empty(),
                CreateThingResponse.class);
        assertNotNull(createThingResponse, FAILURE_CHAR + "Response was null");
        assertNotNull(createThingResponse.getThingID(), FAILURE_CHAR + "thingID was null");
        assertEquals(testThingId, createThingResponse.getThingID(), FAILURE_CHAR + " thingID don't match up");

        // no backend test
        CompletionStage<CreateThingGrpcResponse> mockResponseBad = CompletableFuture.supplyAsync(() ->
                CreateThingGrpcResponse.newBuilder().setErrormessage(BACKEND_UNAVAILABLE)
                        .setMessage("Thing was created").build());

        authenticatedMockHttpExchange(
                CREATE_THING,
                mockResponseBad,
                post("/api/thing"),
                status().isInternalServerError(),
                Optional.of(new CreateThingRequest(testInputAttributes)),
                Optional.of(BACKEND_UNAVAILABLE),
                ErrorResponse.class);

        CompletionStage<CreateThingGrpcResponse> mockResponseEmptyRequest = CompletableFuture.supplyAsync(() ->
                CreateThingGrpcResponse.newBuilder().setThingID(testThingId)
                        .setMessage("Thing was created").build());
        authenticatedMockHttpExchange(
                CREATE_THING,
                mockResponseEmptyRequest,
                post("/api/thing"),
                status().isCreated(),
                Optional.of(new CreateThingRequest(Collections.emptyMap())),
                Optional.empty(),
                CreateThingResponse.class);

        LOGGER.info(SUCCESS_CHAR + "Thing created with thingID {}", createThingResponse.getThingID());

        CompletableFuture<CreateThingGrpcResponse> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException(BACKEND_EXCEPTION));
        authenticatedMockHttpExchange(
                CREATE_THING,
                mockResponseException,
                post("/api/thing"),
                status().isInternalServerError(),
                Optional.of(new CreateThingRequest(testInputAttributes)),
                Optional.of(BACKEND_EXCEPTION),
                ErrorResponse.class);

    }

    @Test
    public void getThingTest() throws Exception {
        // Create
        CompletionStage<CreateThingGrpcResponse> mockCreateResponse = CompletableFuture.supplyAsync(() ->
                CreateThingGrpcResponse.newBuilder().setThingID(testThingId).setMessage("Thing was created").build());
        CreateThingResponse createThingResponse = authenticatedMockHttpExchange(
                CREATE_THING,
                mockCreateResponse,
                post("/api/thing"),
                status().isCreated(),
                Optional.of(new CreateThingRequest(testInputAttributes)),
                Optional.empty(),
                CreateThingResponse.class);
        CompletionStage<GetThingGrpcResponse> mockGetResponse = CompletableFuture.supplyAsync(() ->
                GetThingGrpcResponse.newBuilder().setThingID(testThingId)
                        .putAllAttributes(getProtoMapFromJava(testInputAttributes)).build());

        // Get
        GetThingResponse getThingResponse = authenticatedMockHttpExchange(
                GET_THING,
                mockGetResponse,
                get("/api/thing/{thingID}", createThingResponse.getThingID()),
                status().isOk(),
                Optional.empty(),
                Optional.empty(),
                GetThingResponse.class);

        assertNotNull(getThingResponse, FAILURE_CHAR + "GetThingResponse was null");
        assertTrue(createThingResponse.getThingID().contentEquals(getThingResponse.getThingID()),
                FAILURE_CHAR + "ThingIDs don't match");
        assertTrue(getThingResponse.getAttributes().containsKey(thingNameKey), FAILURE_CHAR + "Attribute not found");
        assertTrue(thingName.contentEquals((String) getThingResponse.getAttributes().get(thingNameKey)),
                FAILURE_CHAR + "Attribute name does not match");

        // bad request test
        CompletionStage<GetThingGrpcResponse> mockGetResponseBad = CompletableFuture.supplyAsync(() ->
                GetThingGrpcResponse.newBuilder().setErrormessage(BACKEND_UNAVAILABLE)
                        .putAllAttributes(getProtoMapFromJava(testInputAttributes)).build());
        authenticatedMockHttpExchange(
                GET_THING,
                mockGetResponseBad,
                get("/api/thing/{thingID}", createThingResponse.getThingID()),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_UNAVAILABLE),
                ErrorResponse.class);

        LOGGER.info(SUCCESS_CHAR + "Obtained thing, attribute map is : {}",
                getThingResponse.getAttributes().toString());

        CompletableFuture<GetThingGrpcResponse> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException(BACKEND_EXCEPTION));
        authenticatedMockHttpExchange(
                GET_THING,
                mockResponseException,
                get("/api/thing/{thingID}", createThingResponse.getThingID()),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_EXCEPTION),
                ErrorResponse.class);

    }

    @Test
    public void updateThingTest() throws Exception {
        // Create
        CompletionStage<CreateThingGrpcResponse> mockCreateResponse = CompletableFuture.supplyAsync(() ->
                CreateThingGrpcResponse.newBuilder().setThingID(testThingId).setMessage("Thing was created").build());
        CreateThingResponse createThingResponse = authenticatedMockHttpExchange(
                CREATE_THING,
                mockCreateResponse,
                post("/api/thing"),
                status().isCreated(),
                Optional.of(new CreateThingRequest(testInputAttributes)),
                Optional.empty(),
                CreateThingResponse.class);

        // Update
        CompletionStage<UpdateThingGrpcResponse> mockUpdateResponse = CompletableFuture.supplyAsync(() ->
                UpdateThingGrpcResponse.newBuilder().setMessage("Thing was updated").build());
        UpdateThingResponse updateThingResponse = authenticatedMockHttpExchange(
                UPDATE_THING,
                mockUpdateResponse,
                put("/api/thing/{thingID}", createThingResponse.getThingID()),
                status().isOk(),
                Optional.of(new UpdateThingRequest(testUpdateAttributes)),
                Optional.empty(),
                UpdateThingResponse.class);

        assertNotNull(updateThingResponse, FAILURE_CHAR + "UpdateThingResponse was null");
        assertTrue("Thing was updated".contentEquals(updateThingResponse.getMessage()), FAILURE_CHAR + "Thing was not updated");

        // Get back
        CompletionStage<GetThingGrpcResponse> mockGetResponse = CompletableFuture.supplyAsync(() ->
                GetThingGrpcResponse.newBuilder().setThingID(testThingId)
                        .putAllAttributes(getProtoMapFromJava(testUpdateAttributes)).build());
        GetThingResponse getThingResponse = authenticatedMockHttpExchange(
                GET_THING,
                mockGetResponse,
                get("/api/thing/{thingID}", createThingResponse.getThingID()),
                status().isOk(),
                Optional.empty(),
                Optional.empty(),
                GetThingResponse.class);

        assertTrue(getThingResponse.getAttributes().containsKey("temp")
                        && (42D == (Double) getThingResponse.getAttributes().get("temp")),
                FAILURE_CHAR + "Thing was not updated correctly. Newly added attribute was not reflected.");

        // bad request test
        CompletionStage<UpdateThingGrpcResponse> mockUpdateResponseBad = CompletableFuture.supplyAsync(() ->
                UpdateThingGrpcResponse.newBuilder().setErrormessage(BACKEND_UNAVAILABLE).build());
        authenticatedMockHttpExchange(
                UPDATE_THING,
                mockUpdateResponseBad,
                put("/api/thing/{thingID}", createThingResponse.getThingID()),
                status().isInternalServerError(),
                Optional.of(new UpdateThingRequest(testUpdateAttributes)),
                Optional.of(BACKEND_UNAVAILABLE),
                ErrorResponse.class);

        CompletionStage<UpdateThingGrpcResponse> mockUpdateResponseEmpty = CompletableFuture.supplyAsync(() ->
                UpdateThingGrpcResponse.newBuilder().setMessage("Thing was updated").build());
        authenticatedMockHttpExchange(
                UPDATE_THING,
                mockUpdateResponseEmpty,
                put("/api/thing/{thingID}", createThingResponse.getThingID()),
                status().isOk(),
                Optional.of(new UpdateThingRequest(Collections.emptyMap())),
                Optional.empty(),
                UpdateThingResponse.class);

        LOGGER.info(SUCCESS_CHAR + "Updated thing.");

        CompletableFuture<UpdateThingGrpcResponse> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException(BACKEND_EXCEPTION));
        authenticatedMockHttpExchange(
                UPDATE_THING,
                mockResponseException,
                put("/api/thing/{thingID}", createThingResponse.getThingID()),
                status().isInternalServerError(),
                Optional.of(new UpdateThingRequest(testUpdateAttributes)),
                Optional.of(BACKEND_EXCEPTION),
                ErrorResponse.class);

    }

    @Test
    public void actorMetricsTest() throws Exception {
        // Metrics
        CompletionStage<GetMetricsGrpcResponse> mockGetMetricsGrpcResponse =
                CompletableFuture.supplyAsync(() ->
                        GetMetricsGrpcResponse
                                .newBuilder()
                                .setCount(42L)
                                .setAveragemessageage(4L)
                                .setTotalmessagesreceived(100L).build());
        GetActorMetricsResponse getActorMetricsResponse = authenticatedMockHttpExchange(
                GET_ACTOR_METRICS,
                mockGetMetricsGrpcResponse,
                get("/api/thing/metrics"),
                status().isOk(),
                Optional.empty(),
                Optional.empty(),
                GetActorMetricsResponse.class);
        assertEquals(42L, (long) getActorMetricsResponse.getActiveThingCount(),
                FAILURE_CHAR + "Actor count did not match up");
        assertEquals(4L, (long) getActorMetricsResponse.getAverageMessageAge(),
                FAILURE_CHAR + "Actor average message age did not match up");
        assertEquals(100L, (long) getActorMetricsResponse.getTotalMessagesReceived(),
                FAILURE_CHAR + "Total messages received did not match up");

        // Metrics
        CompletionStage<GetMetricsGrpcResponse> mockGetMetricsGrpcResponseBad =
                CompletableFuture.supplyAsync(() ->
                        GetMetricsGrpcResponse.newBuilder().setErrormessage(BACKEND_UNAVAILABLE).build());
        authenticatedMockHttpExchange(
                GET_ACTOR_METRICS,
                mockGetMetricsGrpcResponseBad,
                get("/api/thing/metrics"),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_UNAVAILABLE),
                ErrorResponse.class);

        LOGGER.info(SUCCESS_CHAR + "Verified that metrics are correct.");

        CompletableFuture<GetMetricsGrpcResponse> mockGetMetricsGrpcResponseException = new CompletableFuture<>();
        mockGetMetricsGrpcResponseException.completeExceptionally(new RuntimeException(BACKEND_EXCEPTION));
        authenticatedMockHttpExchange(
                GET_ACTOR_METRICS,
                mockGetMetricsGrpcResponseException,
                get("/api/thing/metrics"),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_EXCEPTION),
                ErrorResponse.class);
    }

    @Test
    public void clearThingTest() throws Exception {
        // create
        CompletionStage<CreateThingGrpcResponse> mockCreateResponse = CompletableFuture.supplyAsync(() ->
                CreateThingGrpcResponse.newBuilder().setThingID(testThingId).setMessage("Thing was created").build());
        CreateThingResponse createThingResponse = authenticatedMockHttpExchange(
                CREATE_THING,
                mockCreateResponse,
                post("/api/thing"),
                status().isCreated(),
                Optional.of(new CreateThingRequest(testInputAttributes)),
                Optional.empty(),
                CreateThingResponse.class);

        // clear
        CompletionStage<ClearThingGrpcResponse> mockClearThingResponse = CompletableFuture.supplyAsync(() ->
                ClearThingGrpcResponse.newBuilder().setMessage("Thing was cleared").build());
        ClearThingResponse clearThingResponse = authenticatedMockHttpExchange(
                CLEAR_THING,
                mockClearThingResponse,
                put("/api/thing/clear/{thingID}", createThingResponse.getThingID()),
                status().isOk(),
                Optional.empty(),
                Optional.empty(),
                ClearThingResponse.class);
        assertTrue(clearThingResponse.getMessage().contentEquals("Thing was cleared"),
                FAILURE_CHAR + "Thing was not cleared");
        LOGGER.info(SUCCESS_CHAR + "Cleared thing : {}", createThingResponse.getThingID());

        // bad request test
        CompletionStage<ClearThingGrpcResponse> mockClearThingResponseBad = CompletableFuture.supplyAsync(() ->
                ClearThingGrpcResponse.newBuilder().setErrormessage(BACKEND_UNAVAILABLE).build());
        authenticatedMockHttpExchange(
                CLEAR_THING,
                mockClearThingResponseBad,
                put("/api/thing/clear/{thingID}", createThingResponse.getThingID()),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_UNAVAILABLE),
                ErrorResponse.class);

        CompletableFuture<ClearThingGrpcResponse> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException(BACKEND_EXCEPTION));
        authenticatedMockHttpExchange(
                CLEAR_THING,
                mockResponseException,
                put("/api/thing/clear/{thingID}", createThingResponse.getThingID()),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_EXCEPTION),
                ErrorResponse.class);

    }

    @Test
    public void stopThingTest() throws Exception {
        // create
        CompletionStage<CreateThingGrpcResponse> mockCreateResponse = CompletableFuture.supplyAsync(() ->
                CreateThingGrpcResponse.newBuilder().setThingID(testThingId).setMessage("Thing was created").build());
        CreateThingResponse createThingResponse = authenticatedMockHttpExchange(
                CREATE_THING,
                mockCreateResponse,
                post("/api/thing"),
                status().isCreated(),
                Optional.of(new CreateThingRequest(testInputAttributes)),
                Optional.empty(),
                CreateThingResponse.class);

        // Now stop
        CompletionStage<StopThingGrpcResponse> mockStopThingResponse = CompletableFuture.supplyAsync(() ->
                StopThingGrpcResponse.newBuilder().setMessage("Thing was passivated").build());
        StopThingResponse stopThingResponse = authenticatedMockHttpExchange(
                STOP_THING,
                mockStopThingResponse,
                put("/api/thing/stop/{thingID}", createThingResponse.getThingID()),
                status().isOk(),
                Optional.empty(),
                Optional.empty(),
                StopThingResponse.class);
        assertTrue(stopThingResponse.getMessage().contentEquals("Thing was passivated"), FAILURE_CHAR + "Thing was not stopped");

        // bad request
        CompletionStage<StopThingGrpcResponse> mockStopThingResponseBad = CompletableFuture.supplyAsync(() ->
                StopThingGrpcResponse.newBuilder().setErrormessage(BACKEND_UNAVAILABLE).build());
        authenticatedMockHttpExchange(
                STOP_THING,
                mockStopThingResponseBad,
                put("/api/thing/stop/{thingID}", createThingResponse.getThingID()),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_UNAVAILABLE),
                ErrorResponse.class);

        LOGGER.info(SUCCESS_CHAR + "Stopped thing : {}", createThingResponse.getThingID());

        CompletableFuture<StopThingGrpcResponse> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException(BACKEND_EXCEPTION));
        authenticatedMockHttpExchange(
                STOP_THING,
                mockResponseException,
                put("/api/thing/stop/{thingID}", createThingResponse.getThingID()),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_EXCEPTION),
                ErrorResponse.class);

        CompletableFuture<StopThingGrpcResponse> mockInterruptedException = new CompletableFuture<>();
        mockInterruptedException.completeExceptionally(new InterruptedException(BACKEND_EXCEPTION));
        authenticatedMockHttpExchange(
                STOP_THING,
                mockInterruptedException,
                put("/api/thing/stop/{thingID}", createThingResponse.getThingID()),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_EXCEPTION),
                ErrorResponse.class);
        CompletableFuture<StopThingGrpcResponse> mockExecutionException = new CompletableFuture<>();
        mockExecutionException.completeExceptionally(new ExecutionException(BACKEND_EXCEPTION, new NullPointerException()));
        authenticatedMockHttpExchange(
                STOP_THING,
                mockExecutionException,
                put("/api/thing/stop/{thingID}", createThingResponse.getThingID()),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_EXCEPTION),
                ErrorResponse.class);
    }

    @Test
    public void getAllThingIDsTest() throws Exception {
        Source<StreamAllThingIDsGrpcResponse, NotUsed> mockGetAllThingIDs =
                Source.from(Arrays.stream(new String[]{"a", "b", "c"}).collect(Collectors.toList()))
                        .map(s -> StreamAllThingIDsGrpcResponse.newBuilder().setThingID(s).build());
        GetAllThingIDsResponse getAllThingIDsResponse = authenticatedMockHttpExchange(
                GET_ALL_THING_IDS,
                mockGetAllThingIDs,
                get("/api/thing/ids?maxIDsToReturn=50"),
                status().isOk(),
                Optional.empty(),
                Optional.empty(),
                GetAllThingIDsResponse.class);

        assertEquals(3, getAllThingIDsResponse.getThingIDs().size(), FAILURE_CHAR + "Thing list size is not correct");
        assertTrue(getAllThingIDsResponse.getThingIDs().contains("a"), FAILURE_CHAR + "Expected thing in list was not found");
        assertTrue(getAllThingIDsResponse.getThingIDs().contains("b"), FAILURE_CHAR + "Expected thing in list was not found");
        assertTrue(getAllThingIDsResponse.getThingIDs().contains("c"), FAILURE_CHAR + "Expected thing in list was not found");

        // bad request
        List<StreamAllThingIDsGrpcResponse> l = new ArrayList<>();
        l.add(StreamAllThingIDsGrpcResponse.newBuilder().setErrormessage(BACKEND_UNAVAILABLE).build());
        Source<StreamAllThingIDsGrpcResponse, NotUsed> mockGetAllThingIDsBad = Source.from(l);

        authenticatedMockHttpExchange(
                GET_ALL_THING_IDS,
                mockGetAllThingIDsBad,
                get("/api/thing/ids?maxIDsToReturn=50"),
                status().isInternalServerError(),
                Optional.empty(),
                Optional.of(BACKEND_UNAVAILABLE),
                ErrorResponse.class);

        LOGGER.info(SUCCESS_CHAR + "Retrieved thing list : {}", getAllThingIDsResponse.getThingIDs().toString());
    }
}
