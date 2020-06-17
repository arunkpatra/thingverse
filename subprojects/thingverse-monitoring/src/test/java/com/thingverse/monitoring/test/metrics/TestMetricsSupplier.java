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

package com.thingverse.monitoring.test.metrics;

import com.thingverse.monitoring.test.service.DummyService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import thingverse.monitoring.annotation.MetricsCollector;
import thingverse.monitoring.api.MetricsSupplier;

@MetricsCollector
public class TestMetricsSupplier implements MetricsSupplier {
    private final DummyService dummyService;

    public TestMetricsSupplier(DummyService dummyService) {
        this.dummyService = dummyService;
    }

    @Override
    public Meter[] registerMeters(MeterRegistry meterRegistry) {
        return new Meter[]{
                Counter.builder("thingverse.get_metrics_call_count")
                        .description("Number of GetMetricsData calls.").register(meterRegistry),
                Gauge.builder("thingverse.test_thing_count",
                        () -> dummyService.getMetricData().getCount())
                        .description("Number of test things").register(meterRegistry)};
    }
}
