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
