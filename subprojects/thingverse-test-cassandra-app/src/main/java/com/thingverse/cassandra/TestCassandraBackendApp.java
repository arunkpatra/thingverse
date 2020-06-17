/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.thingverse.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import storage.backend.cassandra.annotation.EnableCassandraStorageBackend;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@EnableCassandraStorageBackend
@PropertySource(value = {"classpath:thingverse-test-cassandra.properties"})
public class TestCassandraBackendApp implements CommandLineRunner {

    private static Logger LOG = LoggerFactory.getLogger(TestCassandraBackendApp.class);

    public static void main(String[] args) {
        SpringApplication.run(TestCassandraBackendApp.class, args);
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Started Cassandra Storage Backend, press Ctrl + C to kill.");
        new CountDownLatch(1).await();
    }
}
