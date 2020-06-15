package com.thingverse.api.health;

import grpc.health.v1.HealthCheckResponse;
import grpc.health.v1.HealthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class ThingverseBackendHealthIndicator extends BackendServerStatus implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseBackendHealthIndicator.class);

    private final HealthClient healthClient;

    public ThingverseBackendHealthIndicator(HealthClient healthClient) {
        this.healthClient = healthClient;
    }

    @Override
    public Health health() {
        if (checkBackendReachability() == 0) {
            return Health.up().build();
        } else {
            return Health.down().withDetail("Error Code", -1).build();
        }
    }

    private int checkBackendReachability() {
        // Find out what the backend says!
        return HealthCheckResponse.ServingStatus.SERVING.equals(getServerStatus(healthClient)) ? 0 : -1;
    }
}
