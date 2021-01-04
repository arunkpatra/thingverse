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

package com.thingverse.common;

import com.thingverse.common.env.health.EnvironmentHealth;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext
@TestPropertySource(locations = {"classpath:application-test.properties"})
public class SampleAppStandaloneModeTests implements IAbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleAppStandaloneModeTests.class);

    @EnvironmentHealth
    private boolean appIsHealthy;

    @Autowired
    private ApplicationContext context;

    @BeforeAll
    // This appears to be the only way to pass a JVM startup property to a Spring test.
    public static void setUp() {
        System.setProperty("operation-mode", "standalone");
    }

    @Test
    public void operationModePropertyAvailabilityTest() {
        // Is property available?
        Assertions.assertTrue(context.getEnvironment().containsProperty("operation-mode"),
                FAILURE_CHAR + "Expected operation-mode to be available in the environment.");
        LOGGER.info(SUCCESS_CHAR + "Property operation-mode was found in environment.");
    }

    @Test
    public void operationModeMatchTest() {
        // Does property value match?
        String opsMode = context.getEnvironment().getProperty("operation-mode", String.class, "not-found");
        Assertions.assertEquals("standalone", opsMode,
                 FAILURE_CHAR + "Was expecting operation-mode to have a value of standalone");
        LOGGER.info(SUCCESS_CHAR + "Property operation-mode had expected value of standalone.");
    }

    @Test
    public void injectedPropertyOverridesTest() {
        // Do we have injected props?
        boolean foo = context.getEnvironment().getProperty("sample.app.standalone.mode.prop.foo", Boolean.class,
                false);
        Assertions.assertTrue(foo,
                FAILURE_CHAR + "Was expecting true for property 'sample.app.standalone.mode.prop.foo'");
        LOGGER.info(SUCCESS_CHAR + "Property 'sample.app.standalone.mode.prop.foo' had expected value of true");

        String bar = context.getEnvironment().getProperty("sample.app.standalone.mode.prop.bar", String.class,
                "baaz");
        Assertions.assertEquals("bar", bar,
                FAILURE_CHAR + "Was expecting 'bar' for property 'sample.app.standalone.mode.prop.bar'");
        LOGGER.info(SUCCESS_CHAR + "Property sample.app.standalone.mode.prop.bar had expected value of bar");
    }
}
