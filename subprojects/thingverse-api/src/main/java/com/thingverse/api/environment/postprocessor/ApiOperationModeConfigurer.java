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

package com.thingverse.api.environment.postprocessor;

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
 * This is a quick way to setup all the properties required for running the API isn cluster mode. Passing a single
 * property via '-Doperation-mode=cluster' will cause the application to run in cluster mode.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ApiOperationModeConfigurer extends AbstractOperationModeOverridesInjector {

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
        thingverseOverrides.put("spring.cloud.consul.enabled", true);
        thingverseOverrides.put("thingverse.grpc.client.discovery-mechanism", "consul");
        thingverseOverrides.put("server.port", 0);

        PropertySource<?> system = environment.getPropertySources().get(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        // What's the runtime? We need to override the override for Kubernetes
        if (null != system && hasRuntimeEnv(system) && null != system.getProperty(RUNTIME_ENVIRONMENT_KEY)) {
            String userSuppliedRuntimeString = (String) system.getProperty(RUNTIME_ENVIRONMENT_KEY);
            if (RUNTIME_ENVIRONMENT_K8S.equalsIgnoreCase(userSuppliedRuntimeString)) {
                // we will pin the port to 9191
                thingverseOverrides.put("server.port", 9191);
                thingverseOverrides.put("spring.cloud.consul.enabled", false);
                thingverseOverrides.put("thingverse.grpc.client.discovery-mechanism", "kubernetes_service");
                logger.info("Kubernetes runtime environment detected. Pinning server port to 9191 for API. " +
                        "Will use DNS as the service discovery method.");
            }
        }

        if (null != system && null != system.getProperty(THINGVERSE_ENV_KEY)) {
            String env = (String) system.getProperty(THINGVERSE_ENV_KEY);
            thingverseOverrides.put("spring.boot.admin.client.instance.metadata.tags.environment", env);
        }

        return thingverseOverrides;
    }
}
