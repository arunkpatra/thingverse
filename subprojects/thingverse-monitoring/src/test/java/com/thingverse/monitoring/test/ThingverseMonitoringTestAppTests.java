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

package com.thingverse.monitoring.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import thingverse.monitoring.service.MeterRegistrar;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ThingverseMonitoringTestAppTests extends AbstractTest {

    private static Logger LOGGER = LoggerFactory.getLogger(ThingverseMonitoringTestAppTests.class);
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    private ApplicationContext context;

    @Test
    public void contextLoads() {
        validateBeanExistence(MeterRegistrar.class);
    }

    // TODO: Fix this test
    @Test
    @Disabled
    public void prometheusActuatorEndpointTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Getting prometheus data /actuator/prometheus GET");
        MvcResult mvcResult = mockMvc.perform(get("/actuator/prometheus").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andReturn();
    }

    // TODO: Fix this test
    @Test
    @Disabled
    public void prometheusMetricsAvailableTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Getting prometheus data /actuator/prometheus GET");
        MvcResult mvcResult = mockMvc.perform(get("/actuator/prometheus").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        String responseDate = mvcResult.getResponse().getContentAsString();
        Assertions.assertTrue(responseDate.contains("thingverse_get_metrics_call_count_total "),
                FAILURE_CHAR + "Response did not contain expected string");
        Assertions.assertTrue(responseDate.contains("thingverse_test_thing_count "),
                FAILURE_CHAR + "Response did not contain expected string");
    }

    private void validateBeanExistence(Class<?>... types) {
        Arrays.stream(types).forEach(t -> {
            if (context.getBeanNamesForType(t).length == 0) {
                Assertions.fail(String.format("Bean of type %s was not found", t.getSimpleName()));
            }
        });
    }
}
