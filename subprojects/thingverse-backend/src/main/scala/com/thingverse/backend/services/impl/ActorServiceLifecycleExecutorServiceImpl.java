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

package com.thingverse.backend.services.impl;

import akka.actor.typed.ActorSystem;
import akka.cluster.typed.Cluster;
import akka.grpc.javadsl.ServerReflection;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Function;
import com.ecwid.consul.v1.Response;
import com.thingverse.backend.services.ActorService;
import com.thingverse.backend.services.ActorServiceLifecycleExecutorService;
import com.thingverse.backend.services.GrpcServerBindingService;
import com.thingverse.backend.services.ManagementService;
import com.thingverse.backend.v1.ThingverseGrpcService;
import com.thingverse.backend.v1.ThingverseGrpcServicePowerApiHandlerFactory;
import grpc.health.v1.GrpcHealthServiceImpl;
import grpc.health.v1.Health;
import grpc.health.v1.HealthHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.commons.util.InetUtils;
import thingverse.discovery.consul.config.ConsulRegistrationProperties;
import thingverse.discovery.consul.service.ConsulRegistrar;

import java.util.*;
import java.util.concurrent.CompletionStage;

import static scala.collection.JavaConverters.setAsJavaSet;

public class ActorServiceLifecycleExecutorServiceImpl implements ActorServiceLifecycleExecutorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActorServiceLifecycleExecutorServiceImpl.class);

    private final ActorSystem<Void> system;
    private final ActorService actorService;
    private final GrpcServerBindingService grpcServerBindingService;
    private final ConsulRegistrar consulRegistrar;
    private final ManagementService managementService;
    private final InetUtils inetUtils;

    public ActorServiceLifecycleExecutorServiceImpl(ActorSystem<Void> system,
                                                    ActorService actorService,
                                                    GrpcServerBindingService grpcServerBindingService,
                                                    ConsulRegistrar consulRegistrar,
                                                    ManagementService managementService,
                                                    InetUtils inetUtils) {
        this.system = system;
        this.actorService = actorService;
        this.grpcServerBindingService = grpcServerBindingService;
        this.consulRegistrar = consulRegistrar;
        this.managementService = managementService;
        this.inetUtils = inetUtils;

        executeLifecycle();
    }

    private void executeLifecycle() {
        // Bind and start Management Server
        ManagementService.ManagementServerInfo mgmtServerInfo = managementService.startManagementServer(system);
        if (mgmtServerInfo.error) {
            LOGGER.error("Management server failure. Exiting...");
            System.exit(-1);
        }
        // Bind gRPC Server
        Function<HttpRequest, CompletionStage<HttpResponse>> thingverseServiceHandler =
                ThingverseGrpcServicePowerApiHandlerFactory.create(actorService, ThingverseGrpcService.name, system.classicSystem());
        Health healthService = new GrpcHealthServiceImpl();
        Function<HttpRequest, CompletionStage<HttpResponse>> healthServiceHandler = HealthHandlerFactory.create(healthService, Health.name, system.classicSystem());
        Function<HttpRequest, CompletionStage<HttpResponse>>[] handlers =
                new Function[]{
                        thingverseServiceHandler,
                        healthServiceHandler,
                        ServerReflection.create(Arrays.asList(ThingverseGrpcService.description, Health.description), system)};
        GrpcServerBindingService.GrpcServerBindingStatuses statuses = grpcServerBindingService.bindAndServe(system, handlers);

        statuses.getServerBindingStatusList().forEach(s -> {
            if (s.error) {
                LOGGER.error("gRPC server failure. Exiting...");
                System.exit(-1);
            }
        });

        // Now register this node service with Consul
        GrpcServerBindingService.GrpcServerBindingStatus httpStatus =
                statuses.getServerBindingStatusList().stream()
                        .findFirst()
                        .filter(s -> s.portName.equalsIgnoreCase("http"))
                        .get();

        if (!consulRegistrar.getProperties().isEnabled()) {
            LOGGER.debug("Invalid call. ConsulRegistrar is not enabled.");
            return;
        }
        ConsulRegistrationProperties props = consulRegistrar.getProperties();
        Map<String, String> metaData = new HashMap<>();
        metaData.put("app_name", "Thingverse");
        metaData.put("version", "0.0.1");

        ConsulRegistrar.ServiceCheckSettings checkSettings = ConsulRegistrar.ServiceCheckSettings
                .create()
                .withGrpc(inetUtils.findFirstNonLoopbackHostInfo().getIpAddress().concat(":").concat(Integer.toString(httpStatus.port)))
                .withGrpcUseTls(false)
                .withInitialStatus(props.getServiceInitialStatus())
                .withInterval(props.getServiceCheckInterval())
                .withHealthCheckCriticalTimeout(props.getHealthCheckCriticalTimeout());

        ConsulRegistrar.ServiceRegistrationSettings settings = ConsulRegistrar.ServiceRegistrationSettings
                .create()
                .withServiceName(props.getServiceName())
                .withId(props.getServiceId())
                .withPort(httpStatus.port)
                .withAddress(inetUtils.findFirstNonLoopbackHostInfo().getIpAddress())
                .withCheckSettings(checkSettings)
                .withMetaData(metaData)
                .withTags(getTagList(props, mgmtServerInfo, httpStatus));

        Response<Void> response = consulRegistrar.register(settings);
        LOGGER.info("Registered new service with these settings: {}. Response was {}", settings, response);
    }

    private List<String> getTagList(ConsulRegistrationProperties props,
                                    ManagementService.ManagementServerInfo mgmtServerInfo,
                                    GrpcServerBindingService.GrpcServerBindingStatus grpcServerInfo) {
        // Put user supplied tags first
        List<String> tagList = new ArrayList<>(props.getTags());
        // system:${spring.application.name},akka-management-port:${akka.management.http.port},grpc-service-port:${thingverse.grpc.server.port}
        tagList.add("system:".concat(props.getServiceName()));
        tagList.add("akka-management-port:".concat(Integer.toString(mgmtServerInfo.port)));
        tagList.add("grpc-service-port:".concat(Integer.toString(grpcServerInfo.port)));
        String allRolesForThisNode = String.join(",", setAsJavaSet(Cluster.get(system).selfMember().roles()));
        tagList.add("thingverse-backend-roles:".concat(allRolesForThisNode));
        String PID = system.settings().config().getString("PID");
        tagList.add("PID:".concat(PID));

        return tagList;
    }
}
