/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.thingverse.common.env.postprocessor;

import com.thingverse.common.env.health.HealthChecker;
import com.thingverse.common.env.health.HealthStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.thingverse.common.env.health.HealthChecker.THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME;
import static com.thingverse.common.env.health.HealthChecker.THINGVERSE_HEALTH_CHECK_SYSTEM_PROPERTY_SOURCE_NAME;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

public abstract class AbstractThingverseEnvChecker implements ThingverseEnvChecker, OnceOnlyEnvProcessor {

    private static final DeferredLog logger = new DeferredLog();

    @Override
    public AtomicInteger executionCount() {
        return new AtomicInteger(0);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
//        if (this.executionCount().getAndIncrement() > 0) {
//            logger.info("This Environment post processor has already executed.");
//            return;
//        }
        logger.info(String.format("%s started post-processing at %s", this.getClass().getSimpleName(),
                (new Date()).toInstant().toString()));
        // Map may be empty
        Map<String, HealthChecker.CheckResult> healthCheckResultMap = runEnvironmentValidationChecks(environment, application);
        injectApplicationHealthStatus(healthCheckResultMap, environment);
        logger.info(String.format("%s completed post-processing at %s", this.getClass().getSimpleName(),
                (new Date()).toInstant().toString()));
    }

    private void injectApplicationHealthStatus(Map<String, HealthChecker.CheckResult> healthCheckResultMap, ConfigurableEnvironment env) {
        Map<String, Object> thingverseAppHealthProperties = new LinkedHashMap<>();

        healthCheckResultMap.forEach((key, value) -> {
            logger.info(String.format("%s status is %s", value.checkedResourceType, value.status));
            thingverseAppHealthProperties.put(
                    THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME.concat(".")
                            .concat(key.toLowerCase()).concat("#").concat(value.checkedResourceType),
                    value.status);
        });
        boolean anyFailures = healthCheckResultMap.entrySet()
                .stream().anyMatch(e -> e.getValue().status.equals(HealthStatus.DOWN));
        thingverseAppHealthProperties.put(THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME, !anyFailures);
        env.getPropertySources()
                .addAfter(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                        new MapPropertySource(THINGVERSE_HEALTH_CHECK_SYSTEM_PROPERTY_SOURCE_NAME, thingverseAppHealthProperties));
    }

    @Override
    public void switchToImmediateLogger() {
        logger.switchTo(this.getClass());
    }

    @Override
    public DeferredLog getLogger() {
        return logger;
    }
}
