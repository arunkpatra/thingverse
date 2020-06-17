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

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.thingverse.backend.actors.RemoteThing;
import com.thingverse.backend.models.AkkaClusterState;
import com.thingverse.backend.models.CreateThing;
import com.thingverse.backend.models.ThingverseActorMetrics;
import com.thingverse.backend.models.UpdateThing;
import com.thingverse.backend.v1.ThingverseGrpcServicePowerApi;

import java.util.concurrent.CompletionStage;

public interface ActorService extends ThingverseGrpcServicePowerApi {

    CompletionStage<RemoteThing.Pong> ping(String thingID);

    CompletionStage<RemoteThing.Confirmation> createThing(CreateThing createThing);

    CompletionStage<RemoteThing.Summary> getThing(String thingID);

    CompletionStage<RemoteThing.PassivationSummary> stopThing(String thingID);

    CompletionStage<RemoteThing.ThingClearedSummary> clearThing(String thingID);

    CompletionStage<RemoteThing.Confirmation> updateThing(UpdateThing updateThing);

    CompletionStage<ThingverseActorMetrics> getActorMetrics();

    CompletionStage<AkkaClusterState> getBackendClusterState();

    Source<String, NotUsed> streamAllThingIDs(long maxIdsToReturn);
}
