package thingverse.kubernetes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KubernetesLookupProperties.class)
@ConditionalOnProperty(prefix = "thingverse.kubernetes", name = {"enabled"}, matchIfMissing = true)
public class KubernetesLookupConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesLookupConfiguration.class);

    private final KubernetesLookupProperties properties;

    public KubernetesLookupConfiguration(KubernetesLookupProperties properties) {
        this.properties = properties;
    }
}
