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

package com.thingverse.monitoring.test.config;

import com.thingverse.monitoring.test.metrics.TestMetricsSupplier;
import com.thingverse.monitoring.test.service.DummyService;
import com.thingverse.monitoring.test.service.DummyServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import thingverse.monitoring.api.MetricsSupplier;

@Configuration
public class TestMetricsConfig {

    @Bean
    public DummyService dummyService() {
        return new DummyServiceImpl();
    }

    @Bean
    public MetricsSupplier metricsSupplier(DummyService dummyService) {
        return new TestMetricsSupplier(dummyService);
    }
}
