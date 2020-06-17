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

package com.thingverse.backend.services;

public interface ThingverseGrpcServiceOperations {

    String GRPC_SERVICE_NAME = "com.thingverse.backend.v1.ThingverseGrpcService";

    String CREATE_THING = GRPC_SERVICE_NAME + "/CreateThing";
    String GET_THING = GRPC_SERVICE_NAME + "/GetThing";
    String STREAM_ALL_THING_IDS = GRPC_SERVICE_NAME + "/StreamAllThingIDs";
    String STOP_THING = GRPC_SERVICE_NAME + "/StopThing";
    String CLEAR_THING = GRPC_SERVICE_NAME + "/ClearThing";
    String UPDATE_THING = GRPC_SERVICE_NAME + "/UpdateThing";
    String GET_METRICS = GRPC_SERVICE_NAME + "/GetMetrics";
    String GET_BACKEND_CLUSTER_STATUS = GRPC_SERVICE_NAME + "/GetBackendClusterStatus";
}
