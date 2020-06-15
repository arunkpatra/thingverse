package thingverse.resilience.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse.resilience")
public class ThingverseResilienceProperties {

    /**
     * Switch to enable or disable resilience.
     */
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
