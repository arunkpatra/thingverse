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

package com.thingverse.grpc.client;

import akka.actor.typed.ActorSystem;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

public class ThingverseGrpcClientTestAppTests extends AbstractTest {

    private static Logger LOGGER = LoggerFactory.getLogger(ThingverseGrpcClientTestAppTests.class);

    @Autowired
    private ApplicationContext context;

    @Test
    public void contextLoads() {
        validateBeanExistence(ActorSystem.class, EnhancedThingverseGrpcServiceClient.class);
        context.getBean(ActorSystem.class);

        LOGGER.info(RUNNING_CHAR + "ActorSystem {} is running.", context.getBean(ActorSystem.class).name());
    }

    private void validateBeanExistence(Class<?>... types) {
        Arrays.stream(types).forEach(t -> {
            if (context.getBeanNamesForType(t).length == 0) {
                Assert.fail(String.format("Bean of type %s was not found", t.getSimpleName()));
            }
        });
    }
}
