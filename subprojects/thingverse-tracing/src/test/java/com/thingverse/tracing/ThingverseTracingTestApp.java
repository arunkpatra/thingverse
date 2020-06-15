package com.thingverse.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import thingverse.tracing.annotation.EnableThingverseTracing;

@SpringBootApplication
@EnableThingverseTracing
@PropertySource(value = {"classpath:application-test.properties"})
public class ThingverseTracingTestApp {

    private static Logger LOGGER = LoggerFactory.getLogger(ThingverseTracingTestApp.class);

    public static void main(String[] args) {
        SpringApplication.run(ThingverseTracingTestApp.class, args);
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }
}
