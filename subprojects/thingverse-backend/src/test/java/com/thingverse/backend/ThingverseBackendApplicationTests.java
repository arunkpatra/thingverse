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

package com.thingverse.backend;

import akka.actor.typed.ActorSystem;
import com.thingverse.api.storage.ThingverseAkkaStorageBackend;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import com.thingverse.backend.services.ActorService;
import com.thingverse.backend.v1.ThingverseGrpcService;
import com.thingverse.common.env.health.EnvironmentHealthListener;
import com.thingverse.common.log.DeferredLogActivator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

public class ThingverseBackendApplicationTests extends AbstractTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void contextLoads() {
        validateBeanExistence(ActorSystem.class, ActorService.class, ThingverseAkkaStorageBackend.class,
                EnhancedThingverseGrpcServiceClient.class, ThingverseGrpcService.class, DeferredLogActivator.class,
                EnvironmentHealthListener.class);
    }

    private void validateBeanExistence(Class<?>... types) {
        Arrays.stream(types).forEach(t -> {
            if (context.getBeanNamesForType(t).length == 0) {
                Assert.fail(String.format("Bean of type %s was not found", t.getName()));
            }
        });
    }
}
