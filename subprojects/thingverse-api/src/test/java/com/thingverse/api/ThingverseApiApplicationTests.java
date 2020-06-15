package com.thingverse.api;

import akka.actor.typed.ActorSystem;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(username = "dummy_user", roles = {"USER"}, password = "password")
public class ThingverseApiApplicationTests extends AbstractTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Environment environment;

    @Test
    public void contextLoads() {
        validateBeanExistence(ActorSystem.class, EnhancedThingverseGrpcServiceClient.class);
    }

    @Test
    public void checkInfoPropTest() {
        Assert.assertTrue(FAILURE_CHAR + " Tag spring.boot.admin.client.instance.metadata.tags.commit not found",
                environment.containsProperty("spring.boot.admin.client.instance.metadata.tags.commit"));
        Assert.assertTrue(FAILURE_CHAR + " Build property spring.boot.admin.client.instance.metadata.tags.version not found",
                environment.containsProperty("spring.boot.admin.client.instance.metadata.tags.version"));
    }

    private void validateBeanExistence(Class<?>... types) {
        for (Class<?> type : types) {
            if (context.getBeanNamesForType(type).length == 0) {
                Assert.fail(String.format("Bean of type %s was not found", type.getSimpleName()));
            }
        }
    }
}
