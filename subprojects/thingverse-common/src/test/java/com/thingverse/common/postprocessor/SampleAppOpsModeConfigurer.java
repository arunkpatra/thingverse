package com.thingverse.common.postprocessor;

import com.thingverse.common.env.postprocessor.AbstractOperationModeOverridesInjector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SampleAppOpsModeConfigurer extends AbstractOperationModeOverridesInjector {

    @Override
    public Map<String, Object> getClusterModeOverrides(ConfigurableEnvironment environment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sample.app.cluster.mode.prop.foo", true);
        map.put("sample.app.cluster.mode.prop.bar", "bar");
        return map;
    }

    @Override
    public Map<String, Object> getStandaloneModeOverrides(ConfigurableEnvironment environment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sample.app.standalone.mode.prop.foo", true);
        map.put("sample.app.standalone.mode.prop.bar", "bar");
        return map;
    }
}
