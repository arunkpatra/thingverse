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

package com.thingverse.common.postprocessor;

import com.thingverse.common.env.health.HealthChecker;
import com.thingverse.common.env.health.HealthChecker.CheckResult;
import com.thingverse.common.env.postprocessor.AbstractThingverseEnvChecker;
import com.thingverse.common.health.DummyHealthChecker;
import com.thingverse.common.health.DummyPoorHealthIndicator;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SampleAppEnvHealthChecker extends AbstractThingverseEnvChecker implements Ordered {
    @Override
    public Map<String, CheckResult> runEnvironmentValidationChecks(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, CheckResult> results = new HashMap<>();
        CheckResult dummyCheckResults = getDummyResourceHealth(environment);
        results.put(dummyCheckResults.checkName, dummyCheckResults);
        CheckResult poorHealth = getDummyPoorHealthIndicator(environment);
        results.put(poorHealth.checkName, poorHealth);
        return results;
    }

    private CheckResult getDummyResourceHealth(ConfigurableEnvironment env) {
        HealthChecker checker = new DummyHealthChecker();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dummy.health.checker.foo", "foo");
        paramMap.put("dummy.health.checker.bar", "bar");

        String appName = env.getProperty("spring.application.name",
                String.class, "???some-default-app-name???");
        paramMap.put("app-name", appName);

        return checker.checkHealth(env, paramMap, getLogger());
    }

    private CheckResult getDummyPoorHealthIndicator(ConfigurableEnvironment env) {
        HealthChecker checker = new DummyPoorHealthIndicator();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("dummy.poor.health.checker.foo", "foo");
        paramMap.put("dummy.poor.health.checker.bar", "bar");

        String appName = env.getProperty("spring.application.name",
                String.class, "???some-default-app-name???");
        paramMap.put("app-name", appName);

        return checker.checkHealth(env, paramMap, getLogger());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 15;
    }
}
