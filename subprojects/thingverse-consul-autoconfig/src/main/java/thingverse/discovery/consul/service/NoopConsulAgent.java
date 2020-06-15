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
