package com.thingverse.storage.backend.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import storage.backend.cassandra.annotation.EnableCassandraStorageBackend;

@SpringBootApplication
@PropertySource(value = {"classpath:application-test.properties"})
@EnableCassandraStorageBackend
public class TestCassandraBackend {

    public static void main(String[] args) {
        SpringApplication.run(TestCassandraBackend.class, args);
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }

}
