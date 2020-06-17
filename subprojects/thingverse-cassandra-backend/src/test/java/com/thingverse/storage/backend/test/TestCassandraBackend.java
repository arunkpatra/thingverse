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
