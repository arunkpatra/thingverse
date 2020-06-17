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

package com.thingverse.storage.backend.test;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSessionRegistry;
import com.thingverse.api.storage.ThingverseAkkaStorageBackend;
import com.thingverse.common.env.health.HealthChecker;
import com.thingverse.common.env.health.HealthStatus;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import storage.backend.cassandra.config.CassandraBackendProperties;
import storage.backend.cassandra.health.CassandraHealthChecker;

import java.util.HashMap;
import java.util.Map;

public class TestCassandraBackendApplicationTests extends AbstractTest {

    private static Logger LOGGER = LoggerFactory.getLogger(TestCassandraBackendApplicationTests.class);
    @Autowired
    ThingverseAkkaStorageBackend storageBackend;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private CassandraBackendProperties properties;
    @Autowired
    private ConfigurableEnvironment configurableEnvironment;

    @Test
    public void contextLoads() {
    }

    @Test
    public void cassandraHealthCheckTest() {
        HealthChecker healthChecker = new CassandraHealthChecker();
        DeferredLog logger = new DeferredLog();
        Map<String, Object> params = new HashMap<>();

        String[] contactPoints = {"localhost:".concat(Integer.toString(properties.getPort()))};
        params.put("cassandra-contact-points", contactPoints);
        HealthChecker.CheckResult result = healthChecker.checkHealth(configurableEnvironment, params, logger);
        Assert.assertEquals(FAILURE_CHAR + "Was expecting UP status", HealthStatus.UP, result.status);
        LOGGER.info(SUCCESS_CHAR + "Cassandra health checker reported UP status.");
    }

    @Test
    public void initStorageBackendTest() {
        // Create an actor system.
        LOGGER.info("Creating the client ActorSystem.");
        ActorSystem<Void> actorSystem =
                ActorSystem.create(Behaviors.empty(), "test-actor-system", ConfigFactory.load());
        LOGGER.info("Create actor system successfully.");
        Map<String, Object> backendContext = new HashMap<>();
        CassandraSession session =
                CassandraSessionRegistry.get(actorSystem).sessionFor("alpakka.cassandra");
        backendContext.put("cassandra-session", session);
        LOGGER.info("@@@@@@@@@@@ Calling init method on backend");
        storageBackend.init(backendContext);
        LOGGER.info("@@@@@@@@@@@ Backend initialized successfully.");
    }
}
