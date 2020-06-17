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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import thingverse.discovery.consul.service.*;

@Configuration
@EnableConfigurationProperties(ConsulRegistrationProperties.class)
public class ConsulRegistrationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulRegistrationConfiguration.class);

    private final ConsulRegistrationProperties properties;

    public ConsulRegistrationConfiguration(ConsulRegistrationProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ConsulRegistrar consulRegistrar(ConsulRegistrationProperties properties, ThingverseConsulAgent agent) {
        return new ConsulRegistrarImpl(properties);
    }

    /**
     * Create the embedded Consul process only if requested.
     *
     * @param props Config properties.
     * @return The agent.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "thingverse.consul.registration", name = {"embedded"}, matchIfMissing = true)
    public ThingverseConsulAgent createEmbeddedConsulProcess(ConsulRegistrationProperties props) {
        return (new EmbeddedConsulAgent(props)).getConsulAgent();
    }

    @Bean
    @ConditionalOnMissingBean(ThingverseConsulAgent.class)
    public ThingverseConsulAgent createNoopConsulProcess(ConsulRegistrationProperties props) {
        return (new NoopConsulAgent(props)).getConsulAgent();
    }
}
