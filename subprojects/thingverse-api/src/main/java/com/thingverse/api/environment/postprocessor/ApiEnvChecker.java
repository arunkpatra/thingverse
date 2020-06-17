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

import com.thingverse.common.env.health.HealthChecker;
import com.thingverse.common.env.health.HealthStatus;
import com.thingverse.common.env.postprocessor.AbstractThingverseEnvChecker;
import com.thingverse.kubernetes.env.health.KubernetesHealthChecker;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;
import thingverse.discovery.consul.config.ConsulRegistrationProperties;
import thingverse.discovery.consul.health.ConsulHealthChecker;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class ApiEnvChecker extends AbstractThingverseEnvChecker {

    private static final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public AtomicInteger executionCount() {
        return counter;
    }

    @Override
    public Map<String, HealthChecker.CheckResult> runEnvironmentValidationChecks(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, HealthChecker.CheckResult> healthCheckResultMap = new HashMap<>();
        String mode = environment.getProperty("operation-mode", String.class, "standalone");
        if (!"cluster".equalsIgnoreCase(mode)) {
            return healthCheckResultMap;
        }

        String runtimeEnv = environment.getProperty("runtime-env", String.class, "local");
        // Consul is used in non-kubernetes environments for service discovery; its the default
        if (!"kubernetes".equalsIgnoreCase(runtimeEnv)) {
            HealthChecker.CheckResult result = getConsulHealth(environment);
            healthCheckResultMap.put(result.checkName, result);

            if (result.status.equals(HealthStatus.DOWN)) {
                Map<String, Object> rejectionOverrides = new LinkedHashMap<>();
                // Stop Consul registration since we know Consul is down.
                rejectionOverrides.put("spring.cloud.consul.enabled", false);
                rejectionOverrides.put("spring.autoconfigure.exclude", excludedAutoConfigs());

                environment.getPropertySources()
                        .addAfter(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                                new MapPropertySource("rejectionOverrides", rejectionOverrides));
            }
        }
        if ("kubernetes".equalsIgnoreCase(runtimeEnv)) {
            HealthChecker.CheckResult k8sHealth = getKubernetesHealth(environment);
            healthCheckResultMap.put(k8sHealth.checkName, k8sHealth);
        }
        return healthCheckResultMap;
    }

    private String excludedAutoConfigs() {
        StringBuffer sb = new StringBuffer();
        sb.append("org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration").append(",")
                .append("org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration").append(",")
                .append("org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration").append(",")
                .append("org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration").append(",")
                .append("org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration").append(",")
                .append("org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration").append(",")
                .append("org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration");

        return sb.toString();
    }

    private HealthChecker.CheckResult getConsulHealth(ConfigurableEnvironment env) {
        HealthChecker checker = new ConsulHealthChecker();
        Map<String, Object> paramMap = new HashMap<>();

        String host = env.getProperty("spring.cloud.consul.host",
                String.class, ConsulRegistrationProperties.CONSUL_DEFAULT_HOST);
        paramMap.put("consul-host", host);

        int port = env.getProperty("spring.cloud.consul.port",
                Integer.class, ConsulRegistrationProperties.CONSUL_DEFAULT_PORT);
        paramMap.put("consul-port", port);

        return checker.checkHealth(env, paramMap, getLogger());
    }

    private HealthChecker.CheckResult getKubernetesHealth(ConfigurableEnvironment env) {
        HealthChecker checker = new KubernetesHealthChecker();
        Map<String, Object> paramMap = new HashMap<>();
        return checker.checkHealth(env, paramMap, getLogger());
    }
}
