package com.thingverse.common.config;

import com.thingverse.common.env.health.ResourcesHealthyCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional({ResourcesHealthyCondition.class})
public class TestConfigUnHealthyCondition {

    @Bean
    DummyBean someDummyBean() {
        return new DummyBean();
    }

    public class DummyBean {
    }
}
