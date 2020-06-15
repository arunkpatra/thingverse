package com.thingverse.api.config;

import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import com.thingverse.backend.v1.GetMetricsGrpcResponse;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.mockito.ArgumentMatchers.any;

@Profile("intg-test")
@Configuration
public class ThingverseServicesTestConfiguration {

    @Bean
    @Primary
    public EnhancedThingverseGrpcServiceClient thingverseGrpcServiceClient() {
        EnhancedThingverseGrpcServiceClient mockedClient = Mockito.mock(EnhancedThingverseGrpcServiceClient.class);

        CompletionStage<GetMetricsGrpcResponse> mockMetricsResponse = CompletableFuture.supplyAsync(() ->
                GetMetricsGrpcResponse.newBuilder()
                        .setTotalmessagesreceived(20L)
                        .setAveragemessageage(1000L)
                        .setCount(42L)
                        .build());
        Mockito.when(mockedClient.getMetrics(any())).thenReturn(mockMetricsResponse);
        Mockito.when(mockedClient.getMetrics(any(), any())).thenReturn(mockMetricsResponse);
        return mockedClient;
    }
}
