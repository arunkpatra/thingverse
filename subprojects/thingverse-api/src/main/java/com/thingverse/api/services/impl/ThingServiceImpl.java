package com.thingverse.api.services.impl;

import akka.actor.typed.ActorSystem;
import akka.stream.javadsl.Sink;
import com.thingverse.api.config.ThingverseApiProperties;
import com.thingverse.api.models.*;
import com.thingverse.api.services.ThingService;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import com.thingverse.backend.v1.*;
import com.thingverse.common.exception.ThingverseBackendException;
import com.thingverse.common.grpc.GrpcStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thingverse.tracing.annotation.Traced;
import thingverse.tracing.config.ThingverseTracer;
import thingverse.tracing.config.ThingverseTracingProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static com.thingverse.api.transformers.ThingverseGrpcResponseTransformers.*;
import static com.thingverse.grpc.ProtoTransformer.getProtoMapFromJava;

public class ThingServiceImpl implements ThingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingServiceImpl.class);
    private static final String THINGVERSE_BACKEND = "thingverseBackend";

    private final EnhancedThingverseGrpcServiceClient client;
    private final ActorSystem<Void> actorSystem;
    private final ThingverseApiProperties properties;
    private final ThingverseTracer thingverseTracer;
    private final ThingverseTracingProperties tracingProperties;

    public ThingServiceImpl(ThingverseApiProperties properties, ThingverseTracingProperties tracingProperties, ActorSystem<Void> actorSystem,
                            EnhancedThingverseGrpcServiceClient client, ThingverseTracer thingverseTracer) {
        this.properties = properties;
        this.actorSystem = actorSystem;
        this.client = client;
        this.tracingProperties = tracingProperties;
        this.thingverseTracer = thingverseTracer;
    }

    @Override
    //@Retry(name = THINGVERSE_BACKEND)
    //@CircuitBreaker(name = THINGVERSE_BACKEND)
    @Traced(operationName = "ThingServiceImpl#createThing", spanType = Traced.SpanType.EXISTING)
    public CreateThingResponse createThing(Map<String, Object> attributes) throws Throwable {
        //LOGGER.info("TRACE>> Sending createThing request to gRPC Client");
        CreateThingGrpcRequest request = CreateThingGrpcRequest.newBuilder()
                .putAllAttributes(getProtoMapFromJava(attributes)).build();
        return toCreateThingResponse(
                client.createThing(request).toCompletableFuture().get(properties.getCallTimeoutMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    //@Retry(name = THINGVERSE_BACKEND)
    //@CircuitBreaker(name = THINGVERSE_BACKEND)
    @Traced(operationName = "ThingServiceImpl#getThing", spanType = Traced.SpanType.EXISTING)
    public GetThingResponse getThing(String thingID) throws Throwable {
        GetThingGrpcRequest request = GetThingGrpcRequest.newBuilder()
                .setThingID(thingID).build();
        return toGetThingResponse(
                client.getThing(request).toCompletableFuture().get(properties.getCallTimeoutMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    //@Retry(name = THINGVERSE_BACKEND)
    //@CircuitBreaker(name = THINGVERSE_BACKEND)
    @Traced(operationName = "ThingServiceImpl#updateThing", spanType = Traced.SpanType.EXISTING)
    public UpdateThingResponse updateThing(String thingID, Map<String, Object> attributes)
            throws Throwable {
        UpdateThingGrpcRequest request = UpdateThingGrpcRequest.newBuilder()
                .setThingID(thingID)
                .putAllAttributes(getProtoMapFromJava(attributes)).build();
        return toUpdateThingResponse(
                client.updateThing(request)
                        .toCompletableFuture().get(properties.getCallTimeoutMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    //@Retry(name = THINGVERSE_BACKEND)
    //@CircuitBreaker(name = THINGVERSE_BACKEND)
    @Traced(operationName = "ThingServiceImpl#getAllThingIDs", spanType = Traced.SpanType.EXISTING)
    public GetAllThingIDsResponse getAllThingIDs(Long maxIDsToReturn) throws Throwable {
        long actualIDsToReturn = 10L;
        if (null != maxIDsToReturn) {
            actualIDsToReturn = maxIDsToReturn;
        }
        return toGetAllThingIDsResponse(
                client.streamAllThingIDs(StreamAllThingIDsGrpcRequest.newBuilder().setMaxidstoreturn(actualIDsToReturn).build())
                        .runWith(Sink.seq(), actorSystem)
                        .toCompletableFuture().get(properties.getCallTimeoutMillis(), TimeUnit.MILLISECONDS)
        );
    }

    @Override
    //@Retry(name = THINGVERSE_BACKEND)
    //@CircuitBreaker(name = THINGVERSE_BACKEND)
    @Traced(operationName = "ThingServiceImpl#stopThing", spanType = Traced.SpanType.EXISTING)
    public StopThingResponse stopThing(String thingID) throws Throwable {
        return toStopThingResponse(
                client.stopThing(StopThingGrpcRequest.newBuilder().setThingID(thingID).build())
                        .toCompletableFuture().get(properties.getCallTimeoutMillis(), TimeUnit.MILLISECONDS)
        );
    }

    @Override
    //@Retry(name = THINGVERSE_BACKEND)
    //@CircuitBreaker(name = THINGVERSE_BACKEND)
    @Traced(operationName = "ThingServiceImpl#clearThing", spanType = Traced.SpanType.EXISTING)
    public ClearThingResponse clearThing(String thingID) throws Throwable {
        return toClearThingResponse(
                client.clearThing(ClearThingGrpcRequest.newBuilder().setThingID(thingID).build())
                        .toCompletableFuture().get(properties.getCallTimeoutMillis(), TimeUnit.MILLISECONDS)
        );
    }

    @Override
    //@Retry(name = THINGVERSE_BACKEND)
    //@CircuitBreaker(name = THINGVERSE_BACKEND)
    @Traced(operationName = "ThingServiceImpl#getActorMetricsResponse", spanType = Traced.SpanType.EXISTING)
    public GetActorMetricsResponse getActorMetricsResponse() throws Throwable {
        return toGetActorMetricsResponse(
                client.getMetrics(GetMetricsGrpcRequest.newBuilder().build())
                        .toCompletableFuture().get(properties.getCallTimeoutMillis(), TimeUnit.MILLISECONDS)
        );
    }

    @Override
    //@Retry(name = THINGVERSE_BACKEND)
    //@CircuitBreaker(name = THINGVERSE_BACKEND)
    @Traced(operationName = "ThingServiceImpl#getBackendClusterStatusResponse", spanType = Traced.SpanType.EXISTING)
    public GetBackendClusterStatusResponse getBackendClusterStatusResponse() throws Throwable {
        return toGetBackendClusterStatusResponse(
                client.getBackendClusterStatus(GetBackendClusterStatusGrpcRequest.newBuilder().build())
                        .toCompletableFuture().get(properties.getCallTimeoutMillis(), TimeUnit.MILLISECONDS)
        );
    }

    private <T> T handleCompletionStage(CompletionStage<T> completionStage,
                                        Function<String, ? extends T> errorResponseBuider)
            throws ThingverseBackendException {
        try {
            return completionStage
                    .exceptionally(throwable -> {
                        if (throwable instanceof io.grpc.StatusRuntimeException) {
                            return errorResponseBuider.apply(
                                    GrpcStatus.from(((io.grpc.StatusRuntimeException) throwable)).toString());
                        } else {
                            return errorResponseBuider.apply(throwable.getMessage());
                        }
                    })
                    .toCompletableFuture().get(properties.getCallTimeoutMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new ThingverseBackendException("Backend operation was interrupted: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new ThingverseBackendException("Backend operation has failed due to: " + e.getCause().getMessage(), e);
        } catch (TimeoutException e) {
            throw new ThingverseBackendException("Backend operation has timed out: " + e.getMessage(), e);
        }
    }

    private Function<String, ? extends List<StreamAllThingIDsGrpcResponse>> thingIdStreamExceptionHandler() {
        return (m) -> {
            List<StreamAllThingIDsGrpcResponse> l = new ArrayList<>();
            l.add(StreamAllThingIDsGrpcResponse.newBuilder().setErrormessage(m).build());
            return l;
        };
    }
}
