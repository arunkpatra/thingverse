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
