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

package com.thingverse.backend.environment.postprocessor;

import com.thingverse.common.env.postprocessor.AbstractOperationModeOverridesInjector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

/**
 * This is a quick way to setup all the properties required for running the Backend in cluster mode. Passing a single
 * property via '-Doperation-mode=cluster' will cause the application to run in cluster mode. You should still pass
 * other parameters s needed to avoid defaults. Those properties are:
 * 1. -Dthingverse.things.remote-thing.thing-timeout-duration=off (To switch off Actor passivation)
 * 2. -Dthingverse.backend.cassandra-contact-points=tp2.local\":\"9043 (Cassandra contact points)
 * 3. -Dthingverse.backend.roles=read-model (CQRS roles for the backend node. Either, read-model, write-model or both - separated by comma.)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class BackendOperationModeConfigurer extends AbstractOperationModeOverridesInjector {

    public static final String BACKEND_ROLE_KEY = "thingverse.backend.roles";
    private static final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public AtomicInteger executionCount() {
        return counter;
    }

    @Override
    public Map<String, Object> getStandaloneModeOverrides(ConfigurableEnvironment environment) {
        Map<String, Object> thingverseOverrides = new LinkedHashMap<>();
        PropertySource<?> system = environment.getPropertySources().get(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        if (null != system && null != system.getProperty(THINGVERSE_ENV_KEY)) {
            String env = (String) system.getProperty(THINGVERSE_ENV_KEY);
            thingverseOverrides.put("spring.boot.admin.client.instance.metadata.tags.environment", env);
        }

        return thingverseOverrides;
    }

    @Override
    public Map<String, Object> getClusterModeOverrides(ConfigurableEnvironment environment) {
        Map<String, Object> thingverseOverrides = new LinkedHashMap<>();
        // Default discovery method is Consul
        thingverseOverrides.put("thingverse.backend.cluster-bootstrap-service-discovery-method", "consul");
        thingverseOverrides.put("thingverse.backend.backend-operation-mode", "cluster");
        thingverseOverrides.put("thingverse.backend.akka-remote-port", 0);
        thingverseOverrides.put("thingverse.backend.grpc-server-port", 0);
        thingverseOverrides.put("thingverse.backend.akka-management-http-port", 0);
        thingverseOverrides.put("thingverse.consul.registration.enabled", true);
        thingverseOverrides.put("thingverse.storage.backend.cassandra.embedded", false);
        thingverseOverrides.put("server.port", 0);

        PropertySource<?> system = environment.getPropertySources().get(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        // What's the runtime? We need to override the override for Kubernetes
        if (null != system && hasRuntimeEnv(system) && null != system.getProperty(RUNTIME_ENVIRONMENT_KEY)) {
            String userSuppliedRuntimeString = (String) system.getProperty(RUNTIME_ENVIRONMENT_KEY);
            if (RUNTIME_ENVIRONMENT_K8S.equalsIgnoreCase(userSuppliedRuntimeString)) {
                // we will pin ports
                thingverseOverrides.put("server.port", 9095);
                thingverseOverrides.put("thingverse.backend.cluster-bootstrap-service-discovery-method", "kubernetes");
                thingverseOverrides.put("thingverse.backend.akka-remote-port", 2551);
                thingverseOverrides.put("thingverse.backend.grpc-server-port", 8080);
                thingverseOverrides.put("thingverse.backend.akka-management-http-port", 8558);
                thingverseOverrides.put("thingverse.consul.registration.enabled", false);
                logger.info("Kubernetes runtime environment detected. Pinned ports: gRPC=8080, remote=2551, management=8558.");
            }
        }
        if (null != system && null != system.getProperty(BACKEND_ROLE_KEY)) {
            String roles = (String) system.getProperty(BACKEND_ROLE_KEY);
            thingverseOverrides.put("spring.boot.admin.client.instance.metadata.tags.roles", roles);
        }

        if (null != system && null != system.getProperty(THINGVERSE_ENV_KEY)) {
            String env = (String) system.getProperty(THINGVERSE_ENV_KEY);
            thingverseOverrides.put("spring.boot.admin.client.instance.metadata.tags.environment", env);
        }

        return thingverseOverrides;
    }
}
