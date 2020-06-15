package com.thingverse.common.health;

import com.thingverse.common.env.health.HealthChecker;
import com.thingverse.common.env.health.HealthStatus;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

public class DummyPoorHealthIndicator implements HealthChecker {
    @Override
    public CheckResult checkHealth(ConfigurableEnvironment env, Map<String, Object> properties, DeferredLog logger) {
        return new CheckResult(this.getClass().getName(), HealthStatus.DOWN, this.getResourceType());
    }

    @Override
    public String getResourceType() {
        return "DUMMY_ETERNALLY_UNHEALTHY_RESOURCE";
    }
}
