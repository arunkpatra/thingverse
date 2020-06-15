package com.thingverse.api.services;

import com.thingverse.api.models.*;

import java.util.Map;

public interface ThingService {

    CreateThingResponse createThing(Map<String, Object> attributes) throws Throwable;

    UpdateThingResponse updateThing(String thingID, Map<String, Object> attributes) throws Throwable;

    GetThingResponse getThing(String thingID) throws Throwable;

    GetAllThingIDsResponse getAllThingIDs(Long maxIDsToReturn) throws Throwable;

    StopThingResponse stopThing(String thingID) throws Throwable;

    ClearThingResponse clearThing(String thingID) throws Throwable;

    GetActorMetricsResponse getActorMetricsResponse() throws Throwable;

    GetBackendClusterStatusResponse getBackendClusterStatusResponse() throws Throwable;
}
