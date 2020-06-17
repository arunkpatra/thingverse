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

import com.thingverse.backend.config.ThingverseBackendProperties;
import com.thingverse.common.env.health.HealthChecker;
import com.thingverse.common.env.health.HealthChecker.CheckResult;
import com.thingverse.common.env.health.HealthStatus;
import com.thingverse.common.env.postprocessor.AbstractThingverseEnvChecker;
import com.thingverse.kubernetes.env.health.KubernetesHealthChecker;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;
import storage.backend.cassandra.health.CassandraHealthChecker;
import thingverse.discovery.consul.config.ConsulRegistrationProperties;
import thingverse.discovery.consul.health.ConsulHealthChecker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class BackendEnvironmentChecker extends AbstractThingverseEnvChecker {

    private static final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public AtomicInteger executionCount() {
        return counter;
    }

    @Override
    public Map<String, CheckResult> runEnvironmentValidationChecks(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, CheckResult> healthCheckResultMap = new HashMap<>();
        String mode = environment.getProperty("operation-mode", String.class, "standalone");
        if (!"cluster".equalsIgnoreCase(mode)) {
            return healthCheckResultMap;
        }

        CheckResult cassandraCheckResult = getCassandraHealth(environment);
        healthCheckResultMap.put(cassandraCheckResult.checkName, cassandraCheckResult);

        String runtimeEnv = environment.getProperty("runtime-env", String.class, "local");
        // Consul is used in non-kubernetes environments for service discovery; its the default
        if (!"kubernetes".equalsIgnoreCase(runtimeEnv)) {
            CheckResult consulCheckResult = getConsulHealth(environment);
            healthCheckResultMap.put(consulCheckResult.checkName, consulCheckResult);
            if (consulCheckResult.status.equals(HealthStatus.DOWN)) {
                Map<String, Object> rejectionOverrides = new LinkedHashMap<>();
                // Stop Consul registration since we know Consul is down.
                rejectionOverrides.put("thingverse.consul.registration.enabled", false);
                rejectionOverrides.put("thingverse.grpc.client.enabled", false);
//            rejectionOverrides.put("spring.autoconfigure.exclude", excludedAutoConfigs());
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
//        sb.append("thingverse.grpc.client.config.ThingverseGrpcClientConfiguration");
        return sb.toString();
    }

    private CheckResult getCassandraHealth(ConfigurableEnvironment env) {

        HealthChecker checker = new CassandraHealthChecker();
        Map<String, Object> paramMap = new HashMap<>();

        String[] contactPoints = env.getProperty("thingverse.backend.cassandra-contact-points",
                String[].class, ThingverseBackendProperties.DEFAULT_CASSANDRA_CONTACT_POINTS);
        paramMap.put("cassandra-contact-points", contactPoints);

        return checker.checkHealth(env, paramMap, getLogger());
    }

    private CheckResult getConsulHealth(ConfigurableEnvironment env) {
        HealthChecker checker = new ConsulHealthChecker();
        Map<String, Object> paramMap = new HashMap<>();

        String host = env.getProperty("thingverse.consul.registration.host",
                String.class, ConsulRegistrationProperties.CONSUL_DEFAULT_HOST);
        paramMap.put("consul-host", host);

        int port = env.getProperty("thingverse.consul.registration.port",
                Integer.class, ConsulRegistrationProperties.CONSUL_DEFAULT_PORT);
        paramMap.put("consul-port", port);

        return checker.checkHealth(env, paramMap, getLogger());
    }

    private HealthChecker.CheckResult getKubernetesHealth(ConfigurableEnvironment env) {
        HealthChecker checker = new KubernetesHealthChecker();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(KubernetesHealthChecker.K8S_THINGVERSE_CHECK_POD_READ_ACCESS_KEY, true);
        String thingverseNamespace = env.getProperty(KubernetesHealthChecker.K8S_THINGVERSE_NAMESPACE_KEY,
                String.class, "thingverse");
        paramMap.put(KubernetesHealthChecker.K8S_THINGVERSE_NAMESPACE_KEY, thingverseNamespace);
        return checker.checkHealth(env, paramMap, getLogger());
    }

    @SuppressWarnings("unused")
    private void dumpProperties(ConfigurableEnvironment environment) {
        StringBuffer sb = new StringBuffer();
        String prefix = "thingverse.backend.";
        String[] props = {"backend-akka-config-file", "roles", "cassandra-contact-points", "dump-actor-system-config",
                "print-initial-actor-system-tree", "grpc-server-host", "grpc-server-port", "akka-remote-port", "akka-management-http-port",
                "actor-timeout-duration", "events-before-snapshotting", "max-snapshots-to-keep", "operation-timeout-duration",
                "node-operation-mode", "akka-management-http-port-min", "akka-management-http-port-max", "actor-system-name"};

        Arrays.stream(props).forEach(p ->
                sb.append("\n")
                        .append("        ")
                        .append("[Prop] : ").append(p).append("=").append(environment.getProperty(prefix + p)));
        getLogger().info("Properties are: " + sb.toString());
    }
}
