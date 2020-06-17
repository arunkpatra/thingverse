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
