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

package thingverse.kubernetes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse.kubernetes")
public class KubernetesLookupProperties {

    private static final String DEFAULT_K8S_POD_LOOKUP_NAMESPACE = "thingverse";
    private static final String DEFAULT_K8S_SERVICE_LOOKUP_NAMESPACE = "thingverse";

    /**
     * Switch to enable/disable auto-configuration.
     */
    private boolean enabled = true;

    /**
     * The K8s pod lookup namespace
     */
    private String podLookupNamespace = DEFAULT_K8S_POD_LOOKUP_NAMESPACE;
    /**
     * The K8s service lookup namespace
     */
    private String serviceLookupNamespace = DEFAULT_K8S_SERVICE_LOOKUP_NAMESPACE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPodLookupNamespace() {
        return podLookupNamespace;
    }

    public void setPodLookupNamespace(String podLookupNamespace) {
        this.podLookupNamespace = podLookupNamespace;
    }

    public String getServiceLookupNamespace() {
        return serviceLookupNamespace;
    }

    public void setServiceLookupNamespace(String serviceLookupNamespace) {
        this.serviceLookupNamespace = serviceLookupNamespace;
    }
}
