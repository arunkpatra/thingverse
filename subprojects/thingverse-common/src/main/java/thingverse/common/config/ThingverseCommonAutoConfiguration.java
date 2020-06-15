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
