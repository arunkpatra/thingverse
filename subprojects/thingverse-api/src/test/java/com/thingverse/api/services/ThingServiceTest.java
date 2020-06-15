package com.thingverse.api.services;

import com.thingverse.api.AbstractTest;
import com.thingverse.api.models.CreateThingResponse;
import com.thingverse.backend.v1.CreateThingGrpcResponse;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import org.junit.Assert;
import org.junit.Test;
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
        Assert.assertNotNull(FAILURE_CHAR + "Response object can not be null.", response.getThingID());

        LOGGER.info(SUCCESS_CHAR + "Thing created with ID {}", response.getThingID());
    }
}
