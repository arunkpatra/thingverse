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

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.typed.Cluster;
import akka.grpc.javadsl.Metadata;
import akka.stream.javadsl.Source;
import com.thingverse.api.command.ThingverseCommand;
import com.thingverse.api.storage.ThingverseAkkaStorageBackend;
import com.thingverse.backend.actors.MetricsCollector;
import com.thingverse.backend.actors.RemoteThing;
import com.thingverse.backend.config.ThingverseBackendProperties;
import com.thingverse.backend.models.*;
import com.thingverse.backend.services.ActorService;
import com.thingverse.backend.services.ThingverseGrpcServiceOperations;
import com.thingverse.backend.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import thingverse.tracing.annotation.Traced;
import thingverse.tracing.config.ThingverseTracer;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static com.thingverse.grpc.ProtoTransformer.getJavaMapFromProto;
import static com.thingverse.grpc.ProtoTransformer.getProtoMapFromJava;

public class ActorServiceImpl implements ActorService, ThingverseGrpcServicePowerApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActorServiceImpl.class);

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ThingverseBackendProperties properties;
    private final ActorSystem<Void> actorSystem;
    private final Duration timeout;
    private final ThingverseAkkaStorageBackend backend;
    private final ClusterSharding sharding;
    private final ThingverseTracer tracer;

    public ActorServiceImpl(ThingverseBackendProperties properties,
                            ActorSystem<Void> actorSystem,
                            ThingverseAkkaStorageBackend backend,
                            ThingverseTracer tracer) {
        this.properties = properties;
        this.actorSystem = actorSystem;
        this.backend = backend;
        this.sharding = ClusterSharding.get(this.actorSystem);
        this.tracer = tracer;
        this.timeout = this.actorSystem.settings().config().getDuration("thingverse.async-operation-timeout-duration");
    }

    @Override
    public CompletionStage<RemoteThing.Pong> ping(String thingID) {
        EntityRef<ThingverseCommand> entityRef =
                sharding.entityRefFor(RemoteThing.ENTITY_TYPE_KEY, thingID);
        return entityRef.ask(RemoteThing.Ping::new, timeout);
    }

    @Override
    public CompletionStage<RemoteThing.Confirmation> createThing(CreateThing data) {
        EntityRef<ThingverseCommand> entityRef =
                sharding.entityRefFor(RemoteThing.ENTITY_TYPE_KEY, data.thingID);
        return entityRef.ask(replyTo -> new RemoteThing.CreateThingCommand(data.thingID, data.attributes, replyTo), timeout);
    }

    @Override
    public Source<String, NotUsed> streamAllThingIDs(long maxIdsToReturn) {
        return backend.getPersistenceIDsSource(actorSystem)
                .filter(p -> p.contains(RemoteThing.class.getSimpleName().concat("|")))
                .take(maxIdsToReturn)
                .map(x -> Objects.requireNonNull(StringUtils.split(x, "|"))[1]);
    }

    @Override
    public CompletionStage<RemoteThing.PassivationSummary> stopThing(String thingID) {
        FindThingResponse findResult = doesThingExist(thingID);
        if (findResult.getThrowable().isPresent()) {
            // An error has occurred
            CompletableFuture<RemoteThing.PassivationSummary> eResp = new CompletableFuture<>();
            eResp.completeExceptionally(findResult.getThrowable().get());
            return eResp;
        } else {
            // No exception
            return findResult.isFound() ?
                    sharding.entityRefFor(RemoteThing.ENTITY_TYPE_KEY, thingID).ask(RemoteThing.GetPassivationSummary::new,
                            timeout) :
                    CompletableFuture.supplyAsync(() -> new RemoteThing.PassivationSummary("Thing not found"));
        }
    }

    /**
     * This assumes that the thing exists. We fire a message and then forget.
     *
     * @param thingID The thing Id
     */
    private void stopThingInternal(String thingID) {
        sharding.entityRefFor(RemoteThing.ENTITY_TYPE_KEY, thingID).tell(RemoteThing.Idle.INSTANCE);
    }

    @Override
    public CompletionStage<RemoteThing.ThingClearedSummary> clearThing(String thingID) {

        FindThingResponse findResult = doesThingExist(thingID);
        if (findResult.getThrowable().isPresent()) {
            // An error has occurred
            CompletableFuture<RemoteThing.ThingClearedSummary> eResp = new CompletableFuture<>();
            eResp.completeExceptionally(findResult.getThrowable().get());
            return eResp;
        } else {
            // No exception
            return findResult.isFound() ?
                    sharding.entityRefFor(RemoteThing.ENTITY_TYPE_KEY, thingID).ask(RemoteThing.ClearThingCommand::new,
                            timeout) :
                    CompletableFuture.supplyAsync(() -> new RemoteThing.ThingClearedSummary("Thing not found"));
        }
    }

    @Override
    public CompletionStage<RemoteThing.Confirmation> updateThing(UpdateThing updateThing) {
        FindThingResponse findResult = doesThingExist(updateThing.thingID);
        if (findResult.getThrowable().isPresent()) {
            // An error has occurred
            CompletableFuture<RemoteThing.Confirmation> eResp = new CompletableFuture<>();
            eResp.completeExceptionally(findResult.getThrowable().get());
            return eResp;
        } else {
            // No exception
            return findResult.isFound() ?
                    sharding.entityRefFor(RemoteThing.ENTITY_TYPE_KEY, updateThing.thingID)
                            .ask(replyTo -> new RemoteThing.UpdateThingCommand(updateThing.thingID,
                                    updateThing.attributes, replyTo), timeout) :
                    CompletableFuture.supplyAsync(() -> new RemoteThing.Rejected("Thing not found"));
        }
    }

    @Override
    public CompletionStage<ThingverseActorMetrics> getActorMetrics() {
        EntityRef<MetricsCollector.Command> entityRef =
                sharding.entityRefFor(MetricsCollector.ENTITY_TYPE_KEY, MetricsCollector.METRICS_COLLECTOR_ENTITY_ID);
        return entityRef.ask(MetricsCollector.GetActorMetrics::new, timeout);
    }

    @Override
    public CompletionStage<AkkaClusterState> getBackendClusterState() {
        List<Member> memberList = new ArrayList<>();
        Iterable<Member> members = Cluster.get(actorSystem).state().getMembers();
        members.forEach(memberList::add);
        boolean allMembersUp = memberList.stream().allMatch(m -> MemberStatus.up().equals(m.status())
                || MemberStatus.weaklyUp().equals(m.status()));
        int totalMembers = memberList.size();
        long readNodeCount = memberList.stream().filter(m -> m.hasRole("read-model")).count();
        long writeNodeCount = memberList.stream().filter(m -> m.hasRole("write-model")).count();
        return CompletableFuture.supplyAsync(() -> new AkkaClusterState(allMembersUp, totalMembers, readNodeCount, writeNodeCount));
    }

    @Override
    public CompletionStage<RemoteThing.Summary> getThing(String thingID) {
        FindThingResponse findResult = doesThingExist(thingID);
        if (findResult.getThrowable().isPresent()) {
            // An error has occurred
            CompletableFuture<RemoteThing.Summary> eResp = new CompletableFuture<>();
            eResp.completeExceptionally(findResult.getThrowable().get());
            return eResp;
        } else {
            // No exception
            return findResult.isFound() ?
                    sharding.entityRefFor(RemoteThing.ENTITY_TYPE_KEY, thingID).ask(RemoteThing.Get::new, timeout) :
                    CompletableFuture.supplyAsync(() -> new RemoteThing.Summary("not-found",
                            Collections.emptyMap()));
        }
    }

    @PreDestroy
    public void shutdownActorSystem() {
        LOGGER.info("Terminating actor system named {}", actorSystem.name());
        actorSystem.terminate();
    }

    private FindThingResponse doesThingExist(String thingID) {
        try {
            boolean found = ping(thingID).toCompletableFuture().get(timeout.toMillis(), TimeUnit.MILLISECONDS).found;
            // now stop that thing that just got created due to the ping (sorry can't find out a way to prevent this)
            if (!found) {
                // this means that the thing with given ID was not found, but an actor was created nevertheless due to
                // the ping. We are trying to passivate this unwanted thing now.
                stopThingInternal(thingID);
            }
            return new FindThingResponse(found, Optional.empty());
        } catch (InterruptedException | TimeoutException e) {
            LOGGER.error("An error occurred, can't check if thing exists. Error is {}", e.getMessage());
            return new FindThingResponse(false, Optional.of(e));
        } catch (ExecutionException e) {
            LOGGER.error("An error occurred, can't check if thing exists. Underlying Error is {}",
                    e.getCause().getMessage());
            return new FindThingResponse(false, Optional.of(e.getCause()));
        }
    }

    // Power methods - start
    @Override
    @Traced(operationName = ThingverseGrpcServiceOperations.CREATE_THING)
    public CompletionStage<CreateThingGrpcResponse> createThing(CreateThingGrpcRequest in, Metadata metadata) {
        return createThing(in);
    }

    @Override
    @Traced(operationName = ThingverseGrpcServiceOperations.GET_THING)
    public CompletionStage<GetThingGrpcResponse> getThing(GetThingGrpcRequest in, Metadata metadata) {
        return getThing(in);
    }

    @Override
    @Traced(operationName = ThingverseGrpcServiceOperations.STREAM_ALL_THING_IDS)
    public Source<StreamAllThingIDsGrpcResponse, NotUsed> streamAllThingIDs(StreamAllThingIDsGrpcRequest in, Metadata metadata) {
        return streamAllThingIDs(in);
    }

    @Override
    @Traced(operationName = ThingverseGrpcServiceOperations.STOP_THING)
    public CompletionStage<StopThingGrpcResponse> stopThing(StopThingGrpcRequest in, Metadata metadata) {
        return stopThing(in);
    }

    @Override
    @Traced(operationName = ThingverseGrpcServiceOperations.CLEAR_THING)
    public CompletionStage<ClearThingGrpcResponse> clearThing(ClearThingGrpcRequest in, Metadata metadata) {
        return clearThing(in);
    }

    @Override
    @Traced(operationName = ThingverseGrpcServiceOperations.UPDATE_THING)
    public CompletionStage<UpdateThingGrpcResponse> updateThing(UpdateThingGrpcRequest in, Metadata metadata) {
        return updateThing(in);
    }

    @Override
    @Traced(operationName = ThingverseGrpcServiceOperations.GET_METRICS)
    public CompletionStage<GetMetricsGrpcResponse> getMetrics(GetMetricsGrpcRequest in, Metadata metadata) {
        return getMetrics(in);
    }

    @Override
    @Traced(operationName = ThingverseGrpcServiceOperations.GET_BACKEND_CLUSTER_STATUS)
    public CompletionStage<GetBackendClusterStatusGrpcResponse> getBackendClusterStatus(GetBackendClusterStatusGrpcRequest in, Metadata metadata) {
        return getBackendClusterStatus(in);
    }
    // Power methods - end

    // No-metadata methods - start
    @Override
    public CompletionStage<CreateThingGrpcResponse> createThing(CreateThingGrpcRequest in) {
        return createThing(new CreateThing(UUID.randomUUID().toString(),
                getJavaMapFromProto(in.getAttributesMap())))
                .handleAsync((confirmation, t) -> {
                    if (null == t) {
                        if (confirmation instanceof RemoteThing.Accepted)
                            return CreateThingGrpcResponse.newBuilder()
                                    .setThingID((((RemoteThing.Accepted) confirmation).summary.thingID))
                                    .setMessage("Thing was created successfully").build();
                        else {
                            return CreateThingGrpcResponse.newBuilder().setThingID("")
                                    .setMessage((((RemoteThing.Rejected) confirmation).reason))
                                    .build();
                        }
                    } else {
                        return CreateThingGrpcResponse.newBuilder().setErrormessage(t.getMessage()).build();
                    }
                });
    }

    @Override
    public CompletionStage<GetThingGrpcResponse> getThing(GetThingGrpcRequest in) {
        return getThing(in.getThingID())
                .handleAsync(((summary, t) -> {
                    if (null == t) {
                        return GetThingGrpcResponse.newBuilder()
                                .setThingID(summary.thingID)
                                .putAllAttributes(getProtoMapFromJava(summary.attributes)).build();
                    } else {
                        return GetThingGrpcResponse.newBuilder().setErrormessage(t.getMessage()).build();
                    }
                }));
    }

    @Override
    public Source<StreamAllThingIDsGrpcResponse, akka.NotUsed> streamAllThingIDs(StreamAllThingIDsGrpcRequest in) {
        return streamAllThingIDs(in.getMaxidstoreturn())
                .map(s -> StreamAllThingIDsGrpcResponse.newBuilder().setThingID(s)
                        .build());
    }

    @Override
    public CompletionStage<StopThingGrpcResponse> stopThing(StopThingGrpcRequest in) {
        return stopThing(in.getThingID())
                .handleAsync((p, t) -> {
                            if (null == t) {
                                return StopThingGrpcResponse.newBuilder().setMessage(p.message).build();
                            } else {
                                return StopThingGrpcResponse.newBuilder().setErrormessage(t.getMessage()).build();
                            }
                        }
                );
    }

    @Override
    public CompletionStage<ClearThingGrpcResponse> clearThing(ClearThingGrpcRequest in) {
        return clearThing(in.getThingID())
                .handleAsync((s, t) -> {
                            if (null == t) {
                                return ClearThingGrpcResponse.newBuilder().setMessage(s.message).build();
                            } else {
                                return ClearThingGrpcResponse.newBuilder().setErrormessage(t.getMessage()).build();
                            }
                        }
                );
    }

    @Override
    public CompletionStage<UpdateThingGrpcResponse> updateThing(UpdateThingGrpcRequest in) {
        return updateThing(new UpdateThing(in.getThingID(),
                getJavaMapFromProto(in.getAttributesMap())))
                .handleAsync((c, t) -> {
                    if (null == t) {
                        if (c instanceof RemoteThing.Accepted) {
                            return UpdateThingGrpcResponse.newBuilder().setMessage("Thing updated").build();
                        } else {
                            return UpdateThingGrpcResponse.newBuilder().setMessage(((RemoteThing.Rejected) c).reason)
                                    .build();
                        }
                    } else {
                        return UpdateThingGrpcResponse.newBuilder().setErrormessage(t.getMessage())
                                .build();
                    }
                });
    }

    @Override
    public CompletionStage<GetMetricsGrpcResponse> getMetrics(GetMetricsGrpcRequest in) {
        return getActorMetrics()
                .handleAsync((c, t) -> {
                            if (t == null) {
                                return GetMetricsGrpcResponse.newBuilder()
                                        .setCount(c.getTotalActiveThings())
                                        .setTotalmessagesreceived(c.getTotalMessagesReceived())
                                        .setAveragemessageage(c.getAverageMessageAge())
                                        .build();
                            } else {
                                return GetMetricsGrpcResponse.newBuilder().setErrormessage(t.getMessage()).build();
                            }
                        }
                );
    }

    @Override
    public CompletionStage<GetBackendClusterStatusGrpcResponse> getBackendClusterStatus(GetBackendClusterStatusGrpcRequest in) {
        return getBackendClusterState()
                .handleAsync((c, t) -> {
                            if (t == null) {
                                return GetBackendClusterStatusGrpcResponse.newBuilder()
                                        .setTotalnodecount(c.getTotalNodeCount())
                                        .setReadnodecount(c.getReadNodeCount())
                                        .setWritenodecount(c.getWriteNodeCount())
                                        .setAllmembershealthy(c.isAllMembersUp())
                                        .build();
                            } else {
                                return GetBackendClusterStatusGrpcResponse.newBuilder().setErrormessage(t.getMessage()).build();
                            }
                        }
                );
    }
    // No-metadata methods - end
}
