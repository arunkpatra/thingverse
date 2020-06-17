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
