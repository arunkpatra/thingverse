package com.thingverse.api.controllers;

import com.thingverse.api.AbstractTest;
import org.junit.Test;
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
