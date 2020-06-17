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
public class SampleAppClusterModeTests implements IAbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleAppClusterModeTests.class);

    @Autowired
    private ApplicationContext context;

    @BeforeClass
    // This appears to be the only way to pass a JVM startup property to a Spring test.
    public static void setUp() {
        System.setProperty("operation-mode", "cluster");
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
        Assert.assertEquals(FAILURE_CHAR + "Was expecting operation-mode to have a value of cluster",
                "cluster", opsMode);
        LOGGER.info(SUCCESS_CHAR + "Property operation-mode had expected value of cluster.");
    }

    @Test
    public void injectedPropertyOverridesTest() {
        // Do we have injected props?
        boolean foo = context.getEnvironment().getProperty("sample.app.cluster.mode.prop.foo", Boolean.class,
                false);
        Assert.assertTrue(FAILURE_CHAR + "Was expecting true for property 'sample.app.cluster.mode.prop.foo'",
                foo);
        LOGGER.info(SUCCESS_CHAR + "Property sample.app.cluster.mode.prop.foo had expected value of true");

        String bar = context.getEnvironment().getProperty("sample.app.cluster.mode.prop.bar", String.class,
                "baaz");
        Assert.assertEquals(FAILURE_CHAR + "Was expecting bar for property 'sample.app.cluster.mode.prop.bar'",
                "bar", bar);
        LOGGER.info(SUCCESS_CHAR + "Property sample.app.cluster.mode.prop.bar had expected value of bar");
    }
}
