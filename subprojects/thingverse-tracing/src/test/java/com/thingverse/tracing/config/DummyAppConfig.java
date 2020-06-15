package com.thingverse.tracing.config;

import com.thingverse.tracing.service.DummyChildService;
import com.thingverse.tracing.service.DummyChildServiceImpl;
import com.thingverse.tracing.service.DummyParentService;
import com.thingverse.tracing.service.DummyParentServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DummyAppConfig {

    @Bean
    public DummyParentService dummyParentService(DummyChildService dummyChildService) {
        return new DummyParentServiceImpl(dummyChildService);
    }

    @Bean
    public DummyChildService dummyChildService() {
        return new DummyChildServiceImpl();
    }
}
