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

package com.thingverse.backend.config;

import akka.actor.typed.ActorSystem;
import com.thingverse.api.storage.ThingverseAkkaStorageBackend;
import com.thingverse.backend.health.AkkaActorSystemHealthIndicator;
import com.thingverse.backend.models.ActorSystemInfo;
import com.thingverse.backend.services.*;
import com.thingverse.backend.services.impl.*;
import com.thingverse.common.env.health.ResourcesHealthyCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.*;
import storage.backend.cassandra.annotation.EnableCassandraStorageBackend;
import thingverse.discovery.consul.annotation.EnableConsulRegistration;
import thingverse.discovery.consul.config.ConsulRegistrationProperties;
import thingverse.discovery.consul.service.ConsulRegistrar;
import thingverse.kubernetes.annotation.EnableKubernetesLookup;
import thingverse.kubernetes.config.KubernetesLookupProperties;
import thingverse.tracing.annotation.EnableThingverseTracing;
import thingverse.tracing.config.ThingverseTracer;

@Configuration
@EnableCassandraStorageBackend
@EnableThingverseTracing
@EnableConsulRegistration
@EnableKubernetesLookup
@EnableConfigurationProperties(ThingverseBackendProperties.class)
@Conditional({ResourcesHealthyCondition.class})
@ComponentScan({"com.thingverse"})
public class ThingverseBackendConfig {

    @Bean
    ThingverseActorSystemManager actorSystemCreator(ThingverseBackendProperties properties,
                                                    KubernetesLookupProperties kubernetesLookupProperties,
                                                    ConsulRegistrationProperties consulRegistrationProperties,
                                                    InetUtils inetUtils,
                                                    ThingverseAkkaStorageBackend storageBackend) {
        return new ThingverseActorSystemManagerImpl(properties, kubernetesLookupProperties,
                consulRegistrationProperties, inetUtils, storageBackend);
    }

    @Primary
    @Bean("thingverseBackendActorSystem")
    ActorSystem<Void> thingverseBackendActorSystem(ActorSystemInfo actorSystemInfo) {
        if (actorSystemInfo.getActorSystem().isPresent()) {
            return actorSystemInfo.getActorSystem().get();
        } else {
            throw new RuntimeException("Failed to get ActorSystem. Aborting.");
        }
    }

    @Bean
    ThingverseAkkaClusterManager thingverseAkkaClusterManager(ActorSystem<Void> actorSystem) {
        return new ThingverseAkkaClusterManagerImpl(actorSystem);
    }

    @Bean("thingverseBackendActorSystemInfo")
    ActorSystemInfo thingverseBackendActorSystemInfo(ThingverseActorSystemManager thingverseActorSystemManager) {
        return thingverseActorSystemManager.createActorSystem();
    }

    @Bean
    AkkaActorSystemHealthIndicator akkaActorSystemHealthIndicator(ActorSystemInfo actorSystemInfo) {
        return new AkkaActorSystemHealthIndicator(actorSystemInfo);
    }

    @Bean
    GrpcServerBindingService grpcServerBindingService(ThingverseBackendProperties properties) {
        return new GrpcServerBindingServiceImpl(properties);
    }

    @Bean
    ManagementService managementService() {
        return new ManagementServiceImpl();
    }

    @Bean
    ClusterBootStrapService clusterBootStrapService(ActorSystem<Void> actorSystem) {
        return new ClusterBootStrapServiceImpl(actorSystem);
    }

    @Bean
    ActorService actorService(ThingverseBackendProperties properties,
                              ActorSystem<Void> actorSystem,
                              ThingverseAkkaStorageBackend backend,
                              ClusterBootStrapService clusterBootStrapService,
                              InetUtils inetUtils, ThingverseTracer tracer) {
        return new ActorServiceImpl(properties, actorSystem, backend, tracer);
    }

    @Bean
    ActorServiceLifecycleExecutorService actorServiceLifecycleExecutorService(ActorSystem<Void> system,
                                                                              ActorService actorService,
                                                                              GrpcServerBindingService grpcServerBindingService,
                                                                              ConsulRegistrar consulRegistrar,
                                                                              ManagementService managementService,
                                                                              ClusterBootStrapService clusterBootStrapService,
                                                                              InetUtils inetUtils) {
        return new ActorServiceLifecycleExecutorServiceImpl(
                system,
                actorService,
                grpcServerBindingService,
                consulRegistrar,
                managementService,
                inetUtils);
    }
}
