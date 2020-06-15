package com.thingverse.discovery.consul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import thingverse.discovery.consul.annotation.EnableConsulRegistration;

@SpringBootApplication
@EnableConsulRegistration
@PropertySource(value = {"classpath:application-test.properties"})
public class TestDiscoveryApp {
    private static Logger LOGGER = LoggerFactory.getLogger(TestDiscoveryApp.class);

    public static void main(String[] args) {
        SpringApplication.run(TestDiscoveryApp.class, args);
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }
}
