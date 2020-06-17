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

package thingverse.discovery.consul.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thingverse.discovery.consul.config.ConsulRegistrationProperties;

public class NoopConsulAgent implements ThingverseConsulAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopConsulAgent.class);

    private final ConsulRegistrationProperties properties;

    public NoopConsulAgent(ConsulRegistrationProperties properties) {
        this.properties = properties;
        LOGGER.debug("NOOP Consul process.");
    }

    public ThingverseConsulAgent getConsulAgent() {
        return this;
    }
}
