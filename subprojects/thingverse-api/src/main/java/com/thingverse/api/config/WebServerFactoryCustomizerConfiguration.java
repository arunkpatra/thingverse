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
