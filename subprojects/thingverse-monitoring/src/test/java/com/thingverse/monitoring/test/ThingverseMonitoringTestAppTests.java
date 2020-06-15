package com.thingverse.monitoring.test;

import org.junit.Assert;
import org.junit.Test;
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

    @Test
    public void prometheusActuatorEndpointTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Getting prometheus data /actuator/prometheus GET");
        MvcResult mvcResult = mockMvc.perform(get("/actuator/prometheus").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void prometheusMetricsAvailableTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Getting prometheus data /actuator/prometheus GET");
        MvcResult mvcResult = mockMvc.perform(get("/actuator/prometheus").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        String responseDate = mvcResult.getResponse().getContentAsString();
        Assert.assertTrue(FAILURE_CHAR + "Response did not contain expected string",
                responseDate.contains("thingverse_get_metrics_call_count_total "));
        Assert.assertTrue(FAILURE_CHAR + "Response did not contain expected string",
                responseDate.contains("thingverse_test_thing_count "));
    }

    private void validateBeanExistence(Class<?>... types) {
        Arrays.stream(types).forEach(t -> {
            if (context.getBeanNamesForType(t).length == 0) {
                Assert.fail(String.format("Bean of type %s was not found", t.getSimpleName()));
            }
        });
    }
}
