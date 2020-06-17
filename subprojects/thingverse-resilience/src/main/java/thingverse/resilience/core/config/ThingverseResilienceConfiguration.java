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

package thingverse.resilience.core.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
@EnableConfigurationProperties(ThingverseResilienceProperties.class)
@ConditionalOnProperty(prefix = "thingverse.grpc.client", name = {"enabled"}, matchIfMissing = true)
public class ThingverseResilienceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseResilienceConfiguration.class);

    private final ThingverseResilienceProperties properties;

    public ThingverseResilienceConfiguration(ThingverseResilienceProperties properties) {
        this.properties = properties;
    }

    @Bean
    RetryConfig thingverseDefaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryExceptions(IOException.class, TimeoutException.class)
                .build();
    }

    @Bean
    RetryRegistry thingverseRetryRegistry(RetryConfig retryConfig) {
        return RetryRegistry.of(retryConfig);
    }

    @Bean("thingverseBackendRetry")
    Retry thingverseBackendRetry(RetryRegistry retryRegistry) {
        return retryRegistry.retry("thingverseBackend");
    }

    @Bean
    CircuitBreakerConfig thingverseCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(3)
                .minimumNumberOfCalls(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .slidingWindowSize(5)
                .recordExceptions(IOException.class, TimeoutException.class)
                .build();
    }

    @Bean
    CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig circuitBreakerConfig) {
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    @Bean("thingverseBackendCircuitBreaker")
    CircuitBreaker circuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
        return circuitBreakerRegistry.circuitBreaker("thingverseBackend");
    }
}
