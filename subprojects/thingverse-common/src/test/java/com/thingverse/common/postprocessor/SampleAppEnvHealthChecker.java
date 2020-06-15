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
