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

import grpc.health.v1.HealthCheckRequest;
import grpc.health.v1.HealthCheckResponse;
import grpc.health.v1.HealthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BackendServerStatus {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackendServerStatus.class);

    public HealthCheckResponse.ServingStatus getServerStatus(HealthClient healthClient) {
        try {
            HealthCheckRequest request = HealthCheckRequest.newBuilder().setService("thingverse-backend").build();
            CompletionStage<HealthCheckResponse> stage = healthClient.check(request);
            HealthCheckResponse response = stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
            LOGGER.info("Backend health check result = {}", response.getStatus().name());
            return response.getStatus();
        } catch (InterruptedException | TimeoutException e) {
            LOGGER.error("An error occurred while seeking backend health. Error message was {}", e.getMessage());
        } catch (ExecutionException e) {
            LOGGER.error("An error occurred while seeking backend health. Error message was {}", e.getCause().getMessage());
        }
        return HealthCheckResponse.ServingStatus.UNKNOWN;
    }
}
