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
