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

package thingverse.discovery.consul.config;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("thingverse.consul.registration")
public class ConsulRegistrationProperties {

    public static final String CONSUL_DEFAULT_HOST = "localhost";
    public static final int CONSUL_DEFAULT_PORT = 8500;

    /**
     * Switch to enable auto-configuration.
     */
    private boolean enabled = false;
    /**
     * Switch to start an embedded Consul server.
     */
    private boolean embedded = false;
    /**
     * The Consul server port to connect to.
     */
    private int port = CONSUL_DEFAULT_PORT;
    /**
     * The Consul server host to connect to. Usually localhost, since Consul must run on each machine in a distributed
     * environment.
     */
    private String host = CONSUL_DEFAULT_HOST;
    /**
     * The name by which the service will be registered.
     */
    private String serviceName = "some-consul-service";
    /**
     * The port of the service to be registered.
     */
    private int servicePort = 8080;
    /**
     * The hostname of the service to be registered.
     */
    private String serviceHost = "127.0.0.1";
    /**
     * Intervals at which Consul should check availability of the service using the configure check(s).
     */
    private String serviceCheckInterval = "3s";
    /**
     * The default service ID. This should be overridden by the user and a unique value should be used.
     */
    private String serviceId = RandomStringUtils.randomNumeric(32);
    /**
     * The initial service status.
     */
    private String serviceInitialStatus = "critical";
    /**
     * The tags to use while registering the service.
     */
    private List<String> tags = new ArrayList<>();

    /**
     * Duration after which critical services will be de-registered automatically. The minimum is 1 minute. The process
     * that reaps critical services runs every 30 seconds.
     */
    private String healthCheckCriticalTimeout = "90s";

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceInitialStatus() {
        return serviceInitialStatus;
    }

    public void setServiceInitialStatus(String serviceInitialStatus) {
        this.serviceInitialStatus = serviceInitialStatus;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceCheckInterval() {
        return serviceCheckInterval;
    }

    public void setServiceCheckInterval(String serviceCheckInterval) {
        this.serviceCheckInterval = serviceCheckInterval;
    }

    public String getServiceHost() {
        return serviceHost;
    }

    public void setServiceHost(String serviceHost) {
        this.serviceHost = serviceHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public String getHealthCheckCriticalTimeout() {
        return healthCheckCriticalTimeout;
    }

    public void setHealthCheckCriticalTimeout(String healthCheckCriticalTimeout) {
        this.healthCheckCriticalTimeout = healthCheckCriticalTimeout;
    }
}
