package com.thingverse.backend.config;

import com.thingverse.backend.services.ActorService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import thingverse.grpc.client.annotation.EnableThingverseGrpcClient;

@Configuration
@EnableThingverseGrpcClient
public class ThingverseNodeTestConfiguration {

    @Bean
    @Primary
    @Profile("mockedActorService")
    public ActorService mockedActorService() {
        return Mockito.mock(ActorService.class);
    }
}
