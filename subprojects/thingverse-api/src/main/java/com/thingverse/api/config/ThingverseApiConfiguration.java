package com.thingverse.api.config;

import akka.actor.typed.ActorSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.thingverse.api.health.ThingverseBackendHealthIndicator;
import com.thingverse.api.repository.UserRepository;
import com.thingverse.api.services.ThingService;
import com.thingverse.api.services.UserService;
import com.thingverse.api.services.impl.ThingServiceImpl;
import com.thingverse.api.services.impl.UserServiceImpl;
import com.thingverse.common.env.health.ResourcesHealthyCondition;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import grpc.health.v1.HealthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import thingverse.grpc.client.annotation.EnableThingverseGrpcClient;
import thingverse.kubernetes.annotation.EnableKubernetesLookup;
import thingverse.monitoring.annotation.EnableThingverseMetrics;
import thingverse.tracing.annotation.EnableThingverseTracing;
import thingverse.tracing.config.ThingverseTracer;
import thingverse.tracing.config.ThingverseTracingProperties;

import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Configuration
@EnableWebMvc
@EnableCaching
@EnableKubernetesLookup
@EnableThingverseTracing
@EnableDiscoveryClient
@EnableThingverseGrpcClient
@EnableThingverseMetrics
@Conditional({ResourcesHealthyCondition.class})
@EnableConfigurationProperties(ThingverseApiProperties.class)
@ComponentScan({"com.thingverse"})
public class ThingverseApiConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseApiConfiguration.class);

    private static final String dateFormat = "yyyy-MM-dd";
    private static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            ObjectMapper objectMapper = builder.build();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            builder.configure(objectMapper);
            builder.simpleDateFormat(dateTimeFormat);
            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(dateFormat)));
            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormat)));
        };
    }

    /**
     * This is an Opt-in, one needs to set thingverse.api.backend-health-check-enabled=true to activate.
     * @param healthClient The health client.
     * @param thingService Thing service.
     * @return The ThingverseBackendHealthIndicator.
     */
    @Bean
    @ConditionalOnProperty(prefix = "thingverse.api", name = "backend-health-check-enabled")
    ThingverseBackendHealthIndicator thingverseBackendHealthIndicator(HealthClient healthClient,
                                                                      ThingService thingService) {
        return new ThingverseBackendHealthIndicator(healthClient);
    }

    @Bean("thingverseBackendService")
    ThingService thingverseThingService(ThingverseApiProperties properties, ThingverseTracingProperties tracingProperties,
                                        ActorSystem<Void> actorSystem,
                                        EnhancedThingverseGrpcServiceClient client, ThingverseTracer thingverseTracer) {
        return new ThingServiceImpl(properties, tracingProperties, actorSystem, client, thingverseTracer);
    }

    @Bean
    CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Collections.singletonList(new ConcurrentMapCache("userByUsername")));
        return cacheManager;
    }

    @Bean
    UserService userService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }

}
