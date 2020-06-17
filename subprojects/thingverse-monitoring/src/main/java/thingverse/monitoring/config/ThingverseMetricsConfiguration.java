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

package thingverse.monitoring.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import thingverse.monitoring.aspects.MeteredAspect;
import thingverse.monitoring.service.MeterRegistrar;
import thingverse.monitoring.service.MeterRegistrarImpl;

@Configuration
@EnableConfigurationProperties(ThingverseMetricsProperties.class)
@ConditionalOnProperty(prefix = "thingverse.metrics", name = {"enabled"})
public class ThingverseMetricsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseMetricsConfiguration.class);

    private final ThingverseMetricsProperties properties;

    public ThingverseMetricsConfiguration(ThingverseMetricsProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean({MeterRegistrar.class})
    public MeterRegistrar meterRegistrar(ApplicationContext applicationContext, MeterRegistry meterRegistry) {
        return new MeterRegistrarImpl(applicationContext, meterRegistry);
    }

    @Bean("meteredAspect")
    public MeteredAspect meteredAspect(MeterRegistrar meterRegistrar) {
        return new MeteredAspect(meterRegistrar);
    }

}
