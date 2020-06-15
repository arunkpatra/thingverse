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
