package com.thingverse.backend.client.v1;

import akka.NotUsed;
import akka.grpc.internal.JavaServerStreamingRequestBuilder;
import akka.grpc.internal.JavaUnaryRequestBuilder;
import akka.grpc.javadsl.SingleResponseRequestBuilder;
import akka.grpc.javadsl.StreamResponseRequestBuilder;
import akka.stream.javadsl.Source;
import com.thingverse.backend.v1.*;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thingverse.tracing.config.ThingverseTracer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * An enhanced version of the {@link ThingverseGrpcService} which wraps a {@link ThingverseGrpcServiceClient}.
 *
 * @author Arun Patra
 */
public class EnhancedThingverseGrpcServiceClientImpl implements EnhancedThingverseGrpcServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedThingverseGrpcServiceClientImpl.class);
    private final ThingverseGrpcServiceClient wrappedClient;
    private final ThingverseTracer thingverseTracer;

    public EnhancedThingverseGrpcServiceClientImpl(ThingverseGrpcServiceClient thingverseGrpcServiceClient,
                                                   ThingverseTracer thingverseTracer) {
        this.wrappedClient = thingverseGrpcServiceClient;
        this.thingverseTracer = thingverseTracer;
    }

    @Override
    public CompletionStage<CreateThingGrpcResponse> createThing(CreateThingGrpcRequest in) {
        return createThing(in, thingverseTracer.extractGrpcMetadataFromExistingSpan());
    }

    @Override
    public Source<StreamAllThingIDsGrpcResponse, NotUsed> streamAllThingIDs(StreamAllThingIDsGrpcRequest in) {
        return streamAllThingIDs(in, thingverseTracer.extractGrpcMetadataFromExistingSpan());
    }

    @Override
    public CompletionStage<StopThingGrpcResponse> stopThing(StopThingGrpcRequest in) {
        return stopThing(in, thingverseTracer.extractGrpcMetadataFromExistingSpan());
    }

    @Override
    public CompletionStage<ClearThingGrpcResponse> clearThing(ClearThingGrpcRequest in) {
        return clearThing(in, thingverseTracer.extractGrpcMetadataFromExistingSpan());
    }

    @Override
    public CompletionStage<UpdateThingGrpcResponse> updateThing(UpdateThingGrpcRequest in) {
        return updateThing(in, thingverseTracer.extractGrpcMetadataFromExistingSpan());
    }

    @Override
    public CompletionStage<GetMetricsGrpcResponse> getMetrics(GetMetricsGrpcRequest in) {
        return getMetrics(in, thingverseTracer.extractGrpcMetadataFromExistingSpan());
    }

    @Override
    public CompletionStage<GetBackendClusterStatusGrpcResponse> getBackendClusterStatus(GetBackendClusterStatusGrpcRequest in) {
        return getBackendClusterStatus(in, thingverseTracer.extractGrpcMetadataFromExistingSpan());
    }

    @Override
    public CompletionStage<GetThingGrpcResponse> getThing(GetThingGrpcRequest in) {
        return getThing(in, thingverseTracer.extractGrpcMetadataFromExistingSpan());
    }

    @Override
    public CompletionStage<CreateThingGrpcResponse> createThing(CreateThingGrpcRequest in, Map<String, String> metadataMap) {
        return addHeaders(wrappedClient.createThing(), metadataMap).invoke(in);
    }

    @Override
    public CompletionStage<GetThingGrpcResponse> getThing(GetThingGrpcRequest in, Map<String, String> metadataMap) {
        return addHeaders(wrappedClient.getThing(), metadataMap).invoke(in);
    }

    @Override
    public Source<StreamAllThingIDsGrpcResponse, NotUsed> streamAllThingIDs(StreamAllThingIDsGrpcRequest in, Map<String, String> metadataMap) {
        return addHeaders(wrappedClient.streamAllThingIDs(), metadataMap).invoke(in);
    }

    @Override
    public CompletionStage<StopThingGrpcResponse> stopThing(StopThingGrpcRequest in, Map<String, String> metadataMap) {
        return addHeaders(wrappedClient.stopThing(), metadataMap).invoke(in);
    }

    @Override
    public CompletionStage<ClearThingGrpcResponse> clearThing(ClearThingGrpcRequest in, Map<String, String> metadataMap) {
        return addHeaders(wrappedClient.clearThing(), metadataMap).invoke(in);
    }

    @Override
    public CompletionStage<UpdateThingGrpcResponse> updateThing(UpdateThingGrpcRequest in, Map<String, String> metadataMap) {
        return addHeaders(wrappedClient.updateThing(), metadataMap).invoke(in);
    }

    @Override
    public CompletionStage<GetMetricsGrpcResponse> getMetrics(GetMetricsGrpcRequest in, Map<String, String> metadataMap) {
        return addHeaders(wrappedClient.getMetrics(), metadataMap).invoke(in);
    }

    @Override
    public CompletionStage<GetBackendClusterStatusGrpcResponse> getBackendClusterStatus(GetBackendClusterStatusGrpcRequest in, Map<String, String> metadataMap) {
        return addHeaders(wrappedClient.getBackendClusterStatus(), metadataMap).invoke(in);
    }

    private <U, V> SingleResponseRequestBuilder<U, V> addHeaders(SingleResponseRequestBuilder<U, V> builder, Map<String, String> metadataMap) {
        for (Map.Entry<String, String> e : metadataMap.entrySet()) {
            builder = builder.addHeader(e.getKey(), e.getValue());
        }
        return enhancedRequestBuilder(builder);
    }

    private <U, V> StreamResponseRequestBuilder<U, V> addHeaders(StreamResponseRequestBuilder<U, V> builder, Map<String, String> metadataMap) {
        for (Map.Entry<String, String> e : metadataMap.entrySet()) {
            builder = builder.addHeader(e.getKey(), e.getValue());
        }
        return enhancedRequestBuilderStream(builder);
    }

    private StreamResponseRequestBuilder enhancedRequestBuilderStream(StreamResponseRequestBuilder builder) {
        if (builder instanceof JavaServerStreamingRequestBuilder) {
            JavaServerStreamingRequestBuilder javaServerStreamingRequestBuilder =
                    (JavaServerStreamingRequestBuilder) builder;
            try {
                String fieldName = "descriptor";
                Field field = javaServerStreamingRequestBuilder.getClass().getDeclaredField(fieldName);
                if (Modifier.isPrivate(field.getModifiers())) {
                    field.setAccessible(true);
                }
                Object desc = field.get(javaServerStreamingRequestBuilder);
                if (desc instanceof MethodDescriptor) {
                    MethodDescriptor md = (MethodDescriptor) desc;
                    builder = builder.addHeader("grpc-method-name", md.getFullMethodName());
                    builder = builder.addHeader("grpc-method-type", md.getType().name());
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException iae) {
                LOGGER.warn("An error occurred while extracting method name: {}", iae.getMessage());
            }
        }
        return builder;
    }

    private SingleResponseRequestBuilder enhancedRequestBuilder(SingleResponseRequestBuilder builder) {
        if (builder instanceof JavaUnaryRequestBuilder) {
            JavaUnaryRequestBuilder javaUnaryRequestBuilder = (JavaUnaryRequestBuilder) builder;
            try {
                String fieldName = "descriptor";
                Field field = javaUnaryRequestBuilder.getClass().getDeclaredField(fieldName);
                if (Modifier.isPrivate(field.getModifiers())) {
                    field.setAccessible(true);
                }
                Object desc = field.get(javaUnaryRequestBuilder);
                if (desc instanceof MethodDescriptor) {
                    MethodDescriptor md = (MethodDescriptor) desc;
                    builder = builder.addHeader("grpc-method-name", md.getFullMethodName());
                    builder = builder.addHeader("grpc-method-type", md.getType().name());
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException iae) {
                LOGGER.warn("An error occurred while extracting method name: {}", iae.getMessage());
            }
        }
        return builder;
    }
}
