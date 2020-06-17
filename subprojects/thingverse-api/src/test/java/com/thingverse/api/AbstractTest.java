package com.thingverse.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thingverse.api.models.ErrorResponse;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles({"intg-test", "embedded-consul"})
public abstract class AbstractTest {
    protected static final String BACKEND_UNAVAILABLE = "Backend Unavailable";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);
    protected static String SUCCESS_CHAR = "✓ ";
    protected static String FAILURE_CHAR = "✕ ";
    protected static String RUNNING_CHAR = "\uD83C\uDFC3 ";
    @Autowired
    protected EnhancedThingverseGrpcServiceClient client;
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected <T, M> T authenticatedMockHttpExchange(MethodCall mCall,
                                                     M mockValue,
                                                     MockHttpServletRequestBuilder requestBuilder,
                                                     ResultMatcher resultMatcher,
                                                     Optional<Object> content,
                                                     Optional<String> expectedErrorMessage,
                                                     Class<T> type) throws Exception {
        Mockito.when(translateMethodCall(mCall)).thenReturn(mockValue);
        requestBuilder.header("Authorization", "Bearer " + obtainAccessToken())
                .contentType(MediaType.APPLICATION_JSON);
        content.ifPresent(c -> requestBuilder.content(asJsonString(c)));
        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        T response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), type);
        expectedErrorMessage.ifPresent(m -> {
            if (response instanceof ErrorResponse) {
                Assert.assertEquals(FAILURE_CHAR + "Did not get expected error description",
                        m, ((ErrorResponse) response).getErrorDescription());
                LOGGER.info(SUCCESS_CHAR + "Receive expected error message: {}", expectedErrorMessage.get());
            }
        });
        return response;
    }

    protected String obtainAccessToken() throws Exception {
        //User user = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        MvcResult mvcResult
                = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new LoginRequest("dummy_user", "password")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        return objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Map.class).get("token").toString();
    }

    private Object translateMethodCall(MethodCall mCall) {
        switch (mCall) {
            case GET_THING:
                return client.getThing(any());
            case CLEAR_THING:
                return client.clearThing(any());
            case CREATE_THING:
                return client.createThing(any());
            case UPDATE_THING:
                return client.updateThing(any());
            case GET_ACTOR_METRICS:
                return client.getMetrics(any());
            case STOP_THING:
                return client.stopThing(any());
            case GET_ALL_THING_IDS:
                return client.streamAllThingIDs(any());
            default:
                return null;
        }
    }

    public enum MethodCall {
        CREATE_THING, CLEAR_THING, UPDATE_THING, GET_THING, STOP_THING, GET_ALL_THING_IDS, GET_ACTOR_METRICS
    }

    protected static class LoginRequest {
        public String username;
        public String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
