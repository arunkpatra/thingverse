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

import com.thingverse.common.env.health.EnvironmentHealthListener;
import com.thingverse.common.env.health.EnvironmentHealthListenerImpl;
import com.thingverse.common.log.DeferredLogActivator;
import com.thingverse.common.log.DeferredLogActivatorImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ThingverseBaseProperties.class)
public class ThingverseCommonAutoConfiguration {

    @Bean
    DeferredLogActivator deferredLogActivator(ApplicationContext context) {
        return new DeferredLogActivatorImpl(context);
    }

    @Bean
    EnvironmentHealthListener environmentHealthListener(ApplicationContext context, ThingverseBaseProperties properties) {
        return new EnvironmentHealthListenerImpl(context, properties);
    }
}
