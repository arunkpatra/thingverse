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
