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

package com.thingverse.backend.client.v1;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.thingverse.backend.v1.*;

import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * A convenience interface that allows supplying metadata for each operation along with the request.
 *
 * @author Arun Patra
 */
public interface EnhancedThingverseGrpcServiceClient extends ThingverseGrpcService {
    CompletionStage<CreateThingGrpcResponse> createThing(CreateThingGrpcRequest in, Map<String, String> metadataMap);

    CompletionStage<GetThingGrpcResponse> getThing(GetThingGrpcRequest in, Map<String, String> metadataMap);

    Source<StreamAllThingIDsGrpcResponse, NotUsed> streamAllThingIDs(StreamAllThingIDsGrpcRequest in, Map<String, String> metadataMap);

    CompletionStage<StopThingGrpcResponse> stopThing(StopThingGrpcRequest in, Map<String, String> metadataMap);

    CompletionStage<ClearThingGrpcResponse> clearThing(ClearThingGrpcRequest in, Map<String, String> metadataMap);

    CompletionStage<UpdateThingGrpcResponse> updateThing(UpdateThingGrpcRequest in, Map<String, String> metadataMap);

    CompletionStage<GetMetricsGrpcResponse> getMetrics(GetMetricsGrpcRequest in, Map<String, String> metadataMap);

    CompletionStage<GetBackendClusterStatusGrpcResponse> getBackendClusterStatus(GetBackendClusterStatusGrpcRequest in, Map<String, String> metadataMap);
}
