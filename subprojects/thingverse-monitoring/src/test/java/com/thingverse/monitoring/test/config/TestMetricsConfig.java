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
