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
