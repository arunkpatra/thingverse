package com.thingverse.grpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import thingverse.discovery.consul.annotation.EnableConsulRegistration;
import thingverse.grpc.client.annotation.EnableThingverseGrpcClient;

@SpringBootApplication
@EnableThingverseGrpcClient
@PropertySource(value = {"classpath:application-test.properties"})
@EnableConsulRegistration
public class ThingverseGrpcClientTestApp {

    private static Logger LOGGER = LoggerFactory.getLogger(ThingverseGrpcClientTestApp.class);

    public static void main(String[] args) {
        SpringApplication.run(ThingverseGrpcClientTestApp.class, args);
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }
}
