package com.thingverse.monitoring.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import thingverse.monitoring.annotation.EnableThingverseMetrics;

@SpringBootApplication
@EnableThingverseMetrics
@PropertySource(value = {"classpath:application-test.properties"})
public class ThingverseMonitoringTestApp {

    private static Logger LOGGER = LoggerFactory.getLogger(ThingverseMonitoringTestApp.class);

    public static void main(String[] args) {
        SpringApplication.run(ThingverseMonitoringTestApp.class, args);
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }
}
