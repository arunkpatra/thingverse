package thingverse.monitoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse.metrics")
public class ThingverseMetricsProperties {

    /**
     * Switch to enable/disable metrics collection.
     */
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
