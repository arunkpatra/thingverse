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
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
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
        assertTrue(retMap.containsKey("username"), FAILURE_CHAR + "Map did to contain user");
        assertTrue(retMap.containsKey("roles"), FAILURE_CHAR + "Map did to contain roles");
        LOGGER.info(SUCCESS_CHAR + "Got user info for dummy_user");
    }
}
