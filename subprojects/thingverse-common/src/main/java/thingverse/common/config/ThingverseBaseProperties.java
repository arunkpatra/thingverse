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

package thingverse.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse")
public class ThingverseBaseProperties {

    /**
     * Should we terminate the app if it's in an unhealthy state?
     */
    private boolean terminateOnAppUnhealthy = true;

    /**
     * Switch to control health check activation. This is an opt-out.
     */
    private boolean healthCheckEnabled = true;

    public boolean isTerminateOnAppUnhealthy() {
        return terminateOnAppUnhealthy;
    }

    public void setTerminateOnAppUnhealthy(boolean terminateOnAppUnhealthy) {
        this.terminateOnAppUnhealthy = terminateOnAppUnhealthy;
    }

    public boolean isHealthCheckEnabled() {
        return healthCheckEnabled;
    }

    public void setHealthCheckEnabled(boolean healthCheckEnabled) {
        this.healthCheckEnabled = healthCheckEnabled;
    }
}
