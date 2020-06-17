package com.thingverse.common.env.postprocessor;


import com.thingverse.common.env.health.HealthChecker;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

public interface ThingverseEnvChecker extends DeferredLogSourceEnvironmentPostProcessor {

    /**
     * Take decisions here.
     *
     * @param environment Environment.
     * @param application Application.
     * @return A HealthStatus map.
     */
    Map<String, HealthChecker.CheckResult> runEnvironmentValidationChecks(ConfigurableEnvironment environment, SpringApplication application);
}
