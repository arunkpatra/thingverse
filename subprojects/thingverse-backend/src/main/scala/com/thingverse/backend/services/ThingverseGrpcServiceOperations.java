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
