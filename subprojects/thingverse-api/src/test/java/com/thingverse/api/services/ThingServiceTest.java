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

package com.thingverse.api.services;

import com.thingverse.api.AbstractTest;
import com.thingverse.api.models.CreateThingResponse;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import com.thingverse.backend.v1.CreateThingGrpcResponse;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.mockito.ArgumentMatchers.any;

@WithMockUser(username = "dummy_user")
public class ThingServiceTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingServiceTest.class);

    @Autowired
    private ThingService thingService;

    @Autowired
    private EnhancedThingverseGrpcServiceClient client;

    @Test
    public void createThing() throws Throwable {
        String thingNameKey = "name";
        String thingName = "Hello Thing";
        Map<String, Object> testInputAttributes = new HashMap<String, Object>() {{
            put(thingNameKey, thingName);
        }};

        CompletionStage<CreateThingGrpcResponse> mockResponse = CompletableFuture.supplyAsync(() ->
                CreateThingGrpcResponse.newBuilder().setThingID("test-thing-id").setMessage("Thing was created").build());

        // Mock the client itself
        Mockito.when(client.createThing(any())).thenReturn(mockResponse);

        CreateThingResponse response = thingService.createThing(testInputAttributes);
        assertNotNull(response.getThingID(), FAILURE_CHAR + "Response object can not be null.");

        LOGGER.info(SUCCESS_CHAR + "Thing created with ID {}", response.getThingID());
    }
}
