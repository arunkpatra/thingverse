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

import com.pszymczyk.consul.ConsulProcess;
import com.pszymczyk.consul.ConsulStarterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thingverse.discovery.consul.config.ConsulRegistrationProperties;

import javax.annotation.PreDestroy;

public class EmbeddedConsulAgent implements ThingverseConsulAgent {
    public static final String CONSUL_VERSION = "1.7.2";
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedConsulAgent.class);
    private final ConsulRegistrationProperties properties;
    private final ConsulProcess consul;

    public EmbeddedConsulAgent(ConsulRegistrationProperties properties) {
        this.properties = properties;
        this.consul = ConsulStarterBuilder.consulStarter()
                .withConsulVersion(CONSUL_VERSION)
                .withHttpPort(this.properties.getPort())
                //.withCustomConfig(customConfiguration)
                .build()
                .start();

        LOGGER.info("Started Embedded Consul Server at: {}", consul.getAddress());
    }

    public ThingverseConsulAgent getConsulAgent() {
        return this;
    }

    @PreDestroy
    public void windUp() {
        try {
            this.consul.close();
        } catch (Throwable t) {
            LOGGER.error("Error while closing consul process {}", t.getMessage());
        }
    }
}
