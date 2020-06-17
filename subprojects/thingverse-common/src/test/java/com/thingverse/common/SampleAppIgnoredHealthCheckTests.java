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
