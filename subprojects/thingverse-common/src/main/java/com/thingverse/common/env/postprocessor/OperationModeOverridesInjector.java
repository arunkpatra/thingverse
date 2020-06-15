package com.thingverse.common.env.postprocessor;

import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

public interface OperationModeOverridesInjector extends DeferredLogSourceEnvironmentPostProcessor {
    Map<String, Object> getClusterModeOverrides(ConfigurableEnvironment environment);

    Map<String, Object> getStandaloneModeOverrides(ConfigurableEnvironment environment);
}
