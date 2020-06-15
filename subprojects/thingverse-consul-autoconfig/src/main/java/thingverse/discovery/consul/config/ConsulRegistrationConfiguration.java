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
