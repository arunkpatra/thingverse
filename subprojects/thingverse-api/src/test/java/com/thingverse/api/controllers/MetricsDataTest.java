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

import com.thingverse.api.AbstractTest;
import com.thingverse.api.models.CreateThingRequest;
import com.thingverse.api.models.CreateThingResponse;
import com.thingverse.api.models.ErrorResponse;
import com.thingverse.backend.v1.CreateThingGrpcResponse;
import com.thingverse.backend.v1.GetMetricsGrpcResponse;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.thingverse.api.AbstractTest.MethodCall.CREATE_THING;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * For matchers docs see:
 * https://www.petrikainulainen.net/programming/spring-framework/integration-testing-of-spring-mvc-applications-write-clean-assertions-with-jsonpath/
 */
@WithMockUser(username = "dummy_user")
public class MetricsDataTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsDataTest.class);
    private final Map<String, Object> testInputAttributes = new HashMap<String, Object>() {{
        String thingName = "Hello Thing";
        String thingNameKey = "name";
        put(thingNameKey, thingName);
    }};

    @Test
    public void getActuatorData() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Getting actuator data via /actuator GET");
        mockMvc.perform(get("/actuator").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }


    @Test
    // TODO: Fix this test
    @Disabled
    public void getActuatorPrometheusData() throws Exception {
        // Create a bunch of thing just for kicks
        createABunchOfThings(10, true); // OK results
        createABunchOfThings(5, false); // Failed results

        // Now get Metrics
        LOGGER.info(RUNNING_CHAR + "Getting prometheus data /actuator/prometheus GET");
        CompletionStage<GetMetricsGrpcResponse> mockMetricsResponse = CompletableFuture.supplyAsync(() ->
                GetMetricsGrpcResponse.newBuilder()
                        .setTotalmessagesreceived(20L)
                        .setAveragemessageage(1000L)
                        .setCount(42L)
                        .build());
        Mockito.when(client.getMetrics(any())).thenReturn(mockMetricsResponse);
        MvcResult mvcResult = mockMvc.perform(get("/actuator/prometheus").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        String responseData = mvcResult.getResponse().getContentAsString();
        System.out.println(responseData);
        assertTrue(responseData.contains("thingverse_active_thing_count"), FAILURE_CHAR + "Response did not contain expected string");
        assertTrue(responseData.contains("thingverse_average_message_age"), FAILURE_CHAR + "Response did not contain expected string");
        assertTrue(responseData.contains("thingverse_total_messages_received"), FAILURE_CHAR + "Response did not contain expected string");
        assertTrue(responseData.contains("thingverse_http_server_request"), FAILURE_CHAR + "Response did not contain expected string");

    }

    private void createABunchOfThings(int N, boolean ok) throws Exception {
        for (int i = 0; i < N; i++) {
            String testThingId = "test-thing-id";
            String localThingID = testThingId.concat("_").concat(String.valueOf(i));
            if (ok) {
                CompletionStage<CreateThingGrpcResponse> mockResponse = CompletableFuture.supplyAsync(() ->
                        CreateThingGrpcResponse.newBuilder().setThingID(localThingID).setMessage("Thing was created").build());
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
                assertEquals(localThingID, createThingResponse.getThingID(), FAILURE_CHAR + " thingID don't match up");
            } else {
                CompletionStage<CreateThingGrpcResponse> mockResponseBad = CompletableFuture.supplyAsync(() ->
                        CreateThingGrpcResponse.newBuilder().setErrormessage(BACKEND_UNAVAILABLE)
                                .setMessage("Thing was not created").build());
                authenticatedMockHttpExchange(
                        CREATE_THING,
                        mockResponseBad,
                        post("/api/thing"),
                        status().isInternalServerError(),
                        Optional.of(new CreateThingRequest(testInputAttributes)),
                        Optional.of(BACKEND_UNAVAILABLE),
                        ErrorResponse.class);
            }
        }
    }
}
