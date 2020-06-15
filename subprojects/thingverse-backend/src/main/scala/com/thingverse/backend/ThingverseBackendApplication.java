package com.thingverse.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@SpringBootApplication
public class ThingverseBackendApplication {

    private static Logger LOGGER = LoggerFactory.getLogger(ThingverseBackendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ThingverseBackendApplication.class, args);
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }
}
