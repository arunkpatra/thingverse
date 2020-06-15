package com.thingverse.common;

import com.thingverse.common.env.health.EnvironmentHealth;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@TestPropertySource(locations = {"classpath:application-test.properties"}, properties = {"thingverse.health-check-enabled=false"})
public class SampleAppIgnoredHealthCheckTests implements IAbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleAppIgnoredHealthCheckTests.class);

    @EnvironmentHealth
    private boolean appIsHealthy;

    @Autowired
    private ApplicationContext context;

    @Test
    public void checkIgnoredHealthCheckTest() {
        // Is app healthy?
        Assert.assertTrue(FAILURE_CHAR + "Expected application to be healthy", appIsHealthy);
        LOGGER.info(SUCCESS_CHAR + "Application is healthy.");
    }
}
