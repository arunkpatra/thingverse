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

package com.thingverse.api.config;

import com.thingverse.common.env.health.ResourcesHealthyCondition;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.SocketUtils;

@Configuration
@Conditional({ResourcesHealthyCondition.class})
public class WebServerFactoryCustomizerConfiguration implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private final Environment environment;
    private final ThingverseApiProperties properties;

    public WebServerFactoryCustomizerConfiguration(Environment environment, ThingverseApiProperties properties) {
        this.environment = environment;
        this.properties = properties;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        if (environment.containsProperty("server.port")) {
            String portString = environment.getProperty("server.port");
            int port = Integer.parseInt(portString);
            if (port != 0) {
                System.getProperties().put("server.port", port);
                return;
            }
        }
        int port = SocketUtils.findAvailableTcpPort(this.properties.getPortNumberMin(),
                this.properties.getPortNumberMax());
        factory.setPort(port);
        System.getProperties().put("server.port", port);
    }
}
