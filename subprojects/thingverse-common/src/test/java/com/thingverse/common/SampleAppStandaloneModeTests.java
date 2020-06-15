package com.thingverse.common;

import com.thingverse.common.env.health.EnvironmentHealth;
import org.junit.Assert;
import org.junit.BeforeClass;
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
@TestPropertySource(locations = {"classpath:application-test.properties"})
public class SampleAppStandaloneModeTests implements IAbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleAppStandaloneModeTests.class);

    @EnvironmentHealth
    private boolean appIsHealthy;

    @Autowired
    private ApplicationContext context;

    @BeforeClass
    // This appears to be the only way to pass a JVM startup property to a Spring test.
    public static void setUp() {
        System.setProperty("operation-mode", "standalone");
    }

    @Test
    public void operationModePropertyAvailabilityTest() {
        // Is property available?
        Assert.assertTrue(FAILURE_CHAR + "Expected operation-mode to be available in the environment.",
                context.getEnvironment().containsProperty("operation-mode"));
        LOGGER.info(SUCCESS_CHAR + "Property operation-mode was found in environment.");
    }

    @Test
    public void operationModeMatchTest() {
        // Does property value match?
        String opsMode = context.getEnvironment().getProperty("operation-mode", String.class, "not-found");
        Assert.assertEquals(FAILURE_CHAR + "Was expecting operation-mode to have a value of standalone", opsMode,
                "standalone");
        LOGGER.info(SUCCESS_CHAR + "Property operation-mode had expected value of standalone.");
    }

    @Test
    public void injectedPropertyOverridesTest() {
        // Do we have injected props?
        boolean foo = context.getEnvironment().getProperty("sample.app.standalone.mode.prop.foo", Boolean.class,
                false);
        Assert.assertTrue(FAILURE_CHAR + "Was expecting true for property 'sample.app.standalone.mode.prop.foo'",
                foo);
        LOGGER.info(SUCCESS_CHAR + "Property 'sample.app.standalone.mode.prop.foo' had expected value of true");

        String bar = context.getEnvironment().getProperty("sample.app.standalone.mode.prop.bar", String.class,
                "baaz");
        Assert.assertEquals(FAILURE_CHAR + "Was expecting 'bar' for property 'sample.app.standalone.mode.prop.bar'",
                "bar", bar);
        LOGGER.info(SUCCESS_CHAR + "Property sample.app.standalone.mode.prop.bar had expected value of bar");
    }
}
