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
