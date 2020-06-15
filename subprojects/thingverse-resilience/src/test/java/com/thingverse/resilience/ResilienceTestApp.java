package com.thingverse.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import thingverse.resilience.core.annotations.EnableThingverseResilience;

@SpringBootApplication
@EnableThingverseResilience
public class ResilienceTestApp {
    private static Logger LOGGER = LoggerFactory.getLogger(ResilienceTestApp.class);

    public static void main(String[] args) {
        SpringApplication.run(ResilienceTestApp.class, args);
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }
}
