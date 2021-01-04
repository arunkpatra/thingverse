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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * For matchers docs see:
 * https://www.petrikainulainen.net/programming/spring-framework/integration-testing-of-spring-mvc-applications-write-clean-assertions-with-jsonpath/
 */
@WithMockUser(username = "dummy_user")
public class AuthControllerTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthControllerTest.class);

    @Test
    public void badAuthenticationTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Authenticating user via endpoint /auth/login POST");
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new LoginRequest("dummy_user", "bad-password")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void goodAuthenticationTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Authenticating user via endpoint /auth/login POST");
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(new LoginRequest("dummy_user", "password")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }
}
