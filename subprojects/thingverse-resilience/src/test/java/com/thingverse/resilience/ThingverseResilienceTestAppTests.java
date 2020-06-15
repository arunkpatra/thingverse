package com.thingverse.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

public class ThingverseResilienceTestAppTests extends AbstractTest {

    private static Logger LOGGER = LoggerFactory.getLogger(ThingverseResilienceTestAppTests.class);

    @Autowired
    private ApplicationContext context;

    @Test
    public void contextLoads() {
        validateBeanExistence(CircuitBreaker.class, Retry.class);
    }

    private void validateBeanExistence(Class<?>... types) {
        Arrays.stream(types).forEach(t -> {
            if (context.getBeanNamesForType(t).length == 0) {
                Assert.fail(String.format("Bean of type %s was not found", t.getSimpleName()));
            }
        });
    }
}
