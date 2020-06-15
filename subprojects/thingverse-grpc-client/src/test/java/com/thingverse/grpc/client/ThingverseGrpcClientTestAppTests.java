package com.thingverse.grpc.client;

import akka.actor.typed.ActorSystem;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;

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
