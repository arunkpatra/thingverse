package com.thingverse.backend.grpc.client;

import akka.actor.typed.ActorSystem;
import akka.grpc.GrpcClientSettings;
import akka.stream.Materializer;
import com.thingverse.backend.AbstractTest;
import grpc.health.v1.HealthCheckRequest;
import grpc.health.v1.HealthCheckResponse;
import grpc.health.v1.HealthClient;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import thingverse.grpc.client.config.ThingverseGrpcClientProperties;

import java.util.concurrent.CompletionStage;

public class BackendServiceLookupViaDiscoveryTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackendServiceLookupViaDiscoveryTest.class);

    @Autowired
    @Qualifier("thingverseApiGrpcClientActorSystem")
    private ActorSystem<Void> actorSystem;
    @Autowired
    private ThingverseGrpcClientProperties properties;

    @Test
    public void checkHealthViaClientConfigTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Running health check test via gRPC - Config discovery.");
        HealthClient healthClient = healthClient("config");
        HealthCheckRequest request = HealthCheckRequest.newBuilder().setService("thingverse-backend").build();
        CompletionStage<HealthCheckResponse> stage = healthClient.check(request);
        HealthCheckResponse response = stage.toCompletableFuture().get();
        Assert.assertEquals(FAILURE_CHAR + "Status did not match", "SERVING", response.getStatus().name());
        LOGGER.info(SUCCESS_CHAR + "Verified health check at gRPC server looked up by Config discovery. " +
                "Server status is = {}", response.getStatus().name());
    }

    @Test
    public void checkHealthViaClientConsulTest() throws Exception {
        LOGGER.info(RUNNING_CHAR + "Running health check test via gRPC - Consul discovery.");
        HealthClient healthClient = healthClient("akka-consul");
        HealthCheckRequest request = HealthCheckRequest.newBuilder().setService("thingverse-backend").build();
        CompletionStage<HealthCheckResponse> stage = healthClient.check(request);
        HealthCheckResponse response = stage.toCompletableFuture().get();
        Assert.assertEquals(FAILURE_CHAR + "Status did not match", "SERVING", response.getStatus().name());
        LOGGER.info(SUCCESS_CHAR + "Verified health check at gRPC server looked up by Consul discovery. " +
                "Server status is = {}", response.getStatus().name());
    }

    private HealthClient healthClient(String method) {
        Materializer materializer = Materializer.matFromSystem(actorSystem.classicSystem());
        if ("config".contentEquals(method)) {
            GrpcClientSettings clientSettings = getSettingsUsingConfigDiscovery();
            LOGGER.info("This test is switching to a Config based discovery. Settings {}", clientSettings);
            return HealthClient.create(clientSettings, materializer,
                    actorSystem.classicSystem().dispatcher());
        }
        if ("akka-consul".contentEquals(method)) {
            GrpcClientSettings clientSettings = getSettingsUsingConsulDiscovery();
            LOGGER.info("This test is switching to a Consul based discovery. Discovery {}", clientSettings.serviceDiscovery().toString());
            return HealthClient.create(clientSettings, materializer,
                    actorSystem.classicSystem().dispatcher());
        }
        throw new UnsupportedOperationException("No other method other than `config` or `akka-consul` is supported.");
    }

    private GrpcClientSettings getSettingsUsingConfigDiscovery() {
        return GrpcClientSettings.fromConfig(properties.getClientName(), actorSystem.classicSystem());
    }

    private GrpcClientSettings getSettingsUsingConsulDiscovery() {
        return GrpcClientSettings
                .usingServiceDiscovery(properties.getServiceName(), actorSystem.classicSystem())
                .withTls(properties.isUseTls());
    }
}
