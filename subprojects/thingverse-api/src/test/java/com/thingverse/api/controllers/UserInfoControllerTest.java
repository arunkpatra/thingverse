package com.thingverse.api.controllers;

import com.thingverse.api.AbstractTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * For matchers docs see:
 * https://www.petrikainulainen.net/programming/spring-framework/integration-testing-of-spring-mvc-applications-write-clean-assertions-with-jsonpath/
 */
@WithMockUser(username = "dummy_user")
public class UserInfoControllerTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoControllerTest.class);

    @SuppressWarnings("rawtypes")
    @Test
    public void createUserInfoTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Getting user info via endpoint /api/me GET");
        MvcResult mvcResult = mockMvc.perform(get("/api/me")
                .header("Authorization", "Bearer " + obtainAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Map retMap = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Map.class);
        Assert.assertTrue(FAILURE_CHAR + "Map did to contain user", retMap.containsKey("username"));
        Assert.assertTrue(FAILURE_CHAR + "Map did to contain roles", retMap.containsKey("roles"));
        LOGGER.info(SUCCESS_CHAR + "Got user info for dummy_user");
    }
}
