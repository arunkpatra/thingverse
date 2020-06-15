package thingverse.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse")
public class ThingverseBaseProperties {

    /**
     * Should we terminate the app if it's in an unhealthy state?
     */
    private boolean terminateOnAppUnhealthy = true;

    /**
     * Switch to control health check activation. This is an opt-out.
     */
    private boolean healthCheckEnabled = true;

    public boolean isTerminateOnAppUnhealthy() {
        return terminateOnAppUnhealthy;
    }

    public void setTerminateOnAppUnhealthy(boolean terminateOnAppUnhealthy) {
        this.terminateOnAppUnhealthy = terminateOnAppUnhealthy;
    }

    public boolean isHealthCheckEnabled() {
        return healthCheckEnabled;
    }

    public void setHealthCheckEnabled(boolean healthCheckEnabled) {
        this.healthCheckEnabled = healthCheckEnabled;
    }
}
