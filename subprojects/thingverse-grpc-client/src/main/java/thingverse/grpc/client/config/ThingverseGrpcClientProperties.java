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

package thingverse.grpc.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse.grpc.client")
public class ThingverseGrpcClientProperties {

    private static final String DEFAULT_CLIENT_NAME = "thingverse-service-client";
    private static final String DEFAULT_SERVICE_NAME = "thingverse-backend";

    /**
     * Switch to enable/disable auto-configuration.
     */
    private boolean enabled = true;
    /**
     * The service discovery mechanism to use.
     */
    private ServiceDiscoveryMechanism discoveryMechanism = ServiceDiscoveryMechanism.STATIC;
    /**
     * The client name to use.
     */
    private String clientName = DEFAULT_CLIENT_NAME;
    /**
     * The gRPC Service to look for.
     */
    private String serviceName = DEFAULT_SERVICE_NAME;
    /**
     * Whether to use TLS for gRPC communication
     */
    private boolean useTls = false;

    public boolean isUseTls() {
        return useTls;
    }

    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public ServiceDiscoveryMechanism getDiscoveryMechanism() {
        return discoveryMechanism;
    }

    public void setDiscoveryMechanism(ServiceDiscoveryMechanism discoveryMechanism) {
        this.discoveryMechanism = discoveryMechanism;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
