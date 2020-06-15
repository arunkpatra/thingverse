package thingverse.tracing.aspects;

import akka.Done;
import akka.grpc.javadsl.Metadata;
import akka.grpc.javadsl.MetadataEntry;
import akka.grpc.javadsl.StringEntry;
import akka.japi.Pair;
import akka.japi.function.Function2;
import akka.stream.javadsl.Source;
import com.thingverse.common.grpc.GrpcStatus;
import com.thingverse.common.utils.TracingCandidateExceptionExtractor;
import io.grpc.StatusRuntimeException;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapAdapter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import thingverse.tracing.annotation.TraceTag;
import thingverse.tracing.annotation.Traced;
import thingverse.tracing.config.ThingverseTracer;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Aspect to proxy Traced methods.
 *
 * @author Arun Patra
 */
@Aspect
public class TracedAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(TracedAspect.class);
    private final ThingverseTracer tracer;

    public TracedAspect(ThingverseTracer tracer) {
        LOGGER.trace("Creating TracedAspect aspect.");
        this.tracer = tracer;
    }

    @Pointcut("@annotation(thingverse.tracing.annotation.Traced)")
    public void tracedTarget() {
    }

    @Around(value = "tracedTarget()")
    public Object submit(ProceedingJoinPoint joinPoint) throws Throwable {
//        if (!tracer.enabled) {
//            // Silently exit, without making any noise whatsoever.
//            return joinPoint.proceed();
//        }
        Traced traced = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Traced.class);
        LOGGER.trace("[Trace: #{}] Starting trace.", traced.operationName());
        switch (traced.spanType()) {
            case PROPAGATED:
                return handleWithPropagatedParentSpan(traced, joinPoint);
            case EXISTING:
                return handleWithLocalParentSpan(traced, joinPoint);
            default:
                // New span will be created
                return handleNewSpan(traced, joinPoint);
        }
    }

    /**
     * Create a brand new span.
     *
     * @param traced    The traced annotation
     * @param joinPoint The joinPoint
     * @return The result of the underlying operation proxied by the aspect.
     * @throws Throwable Throwable if any exception is thrown.
     */
    private Object handleNewSpan(Traced traced, ProceedingJoinPoint joinPoint) throws Throwable {
        LOGGER.trace("[Trace: #{}] Will create new span.", traced.operationName());
        String operationName = traced.operationName();
        Span currentSpan = enhanceSpanFromRequest(traced, tracer.tracer.buildSpan(operationName).start(), joinPoint);
        LOGGER.trace("[Trace: #{}] Started new span -> spanId={}, traceId={}", traced.operationName(),
                currentSpan.context().toSpanId(), currentSpan.context().toTraceId());
        return invokeUnderlyingOperationInScope(traced, currentSpan, joinPoint);
    }

    /**
     * Create a child span of a locally(in the same process) available Span.
     *
     * @param traced    The traced annotation
     * @param joinPoint The joinPoint
     * @return The result of the underlying operation proxied by the aspect.
     * @throws Throwable Throwable if any exception is thrown.
     */
    private Object handleWithLocalParentSpan(Traced traced, ProceedingJoinPoint joinPoint) throws Throwable {
        LOGGER.trace("[Trace: #{}] Will try to use existing span as parent span.", traced.operationName());
        String operationName = traced.operationName();
        Span ps = tracer.tracer.activeSpan();
        if (ps != null) {
            SpanContext parentSpanContext = ps.context();
            Span currentSpan =
                    enhanceSpanFromRequest(traced, tracer.tracer.buildSpan(operationName).asChildOf(parentSpanContext).start(),
                            joinPoint);
            LOGGER.trace("[Trace: #{}] Started child span of existing propagated span. Parent spanId={}, " +
                            "Parent traceId={}, Child spanId={}. Child traceId={}",
                    traced.operationName(),
                    parentSpanContext.toSpanId(),
                    parentSpanContext.toTraceId(),
                    currentSpan.context().toSpanId(),
                    currentSpan.context().toTraceId());
            return invokeUnderlyingOperationInScope(traced, currentSpan, joinPoint);
        } else {
            LOGGER.trace("[Trace: #{}] No parent span exists. Will create new span", traced.operationName());
            return handleNewSpan(traced, joinPoint);
        }
    }

    /**
     * Create a child span of a parent span whose spanContext has been propagated across process boundaries, probably
     * using HTTP headers or gRPC Metadata. The spanContext must have valid data for the underlying tracing technology
     * used (e.g Jaeger).
     *
     * @param traced    The traced annotation
     * @param joinPoint The joinPoint
     * @return The result of the underlying operation proxied by the aspect.
     * @throws Throwable Throwable if any exception is thrown.
     */
    private Object handleWithPropagatedParentSpan(Traced traced, ProceedingJoinPoint joinPoint) throws Throwable {
        LOGGER.trace("[Trace: #{}] Will try to use existing span as parent span.", traced.operationName());
        SpanContext propagatedContext = tracer.tracer.extract(Format.Builtin.HTTP_HEADERS,
                getCarrier(traced, joinPoint));
        if (null != propagatedContext) {
            LOGGER.trace("[Trace: #{}] Extracted propagated context -> spanId={}, traceId={}", traced.operationName(),
                    propagatedContext.toSpanId(), propagatedContext.toTraceId());
            Span currentSpan =
                    enhanceSpanFromRequest(traced, tracer.tracer.buildSpan(traced.operationName()).asChildOf(propagatedContext).start(),
                            joinPoint);
            LOGGER.trace("[Trace: #{}] Started child span with propagated parent context. Parent spanId={}, " +
                            "Parent traceId={}, Child spanId={}. Child traceId={}",
                    traced.operationName(),
                    propagatedContext.toSpanId(),
                    propagatedContext.toTraceId(),
                    currentSpan.context().toSpanId(),
                    currentSpan.context().toTraceId()
            );
            return invokeUnderlyingOperationInScope(traced, currentSpan, joinPoint);
        } else {
            LOGGER.trace("[Trace: #{}] Failed to extract propagated context. Will try with local parent span.", traced.operationName());
            return handleWithLocalParentSpan(traced, joinPoint);
        }
    }

    private Object invokeUnderlyingOperationInScope(Traced traced, Span currentSpan, ProceedingJoinPoint joinPoint) throws Throwable {
        // Scope will be auto-closed
        try (Scope scope = tracer.tracer.activateSpan(currentSpan)) {
            Object proceed;
            // Do real stuff
            LOGGER.trace("[Trace: #{}] Invoking underlying operation", traced.operationName());
            proceed = joinPoint.proceed();
            //currentSpan = enhanceSpanFromResponse2(currentSpan, proceed);
            // Finish span if one exists
            // The span is enhanced internally with exception data
            proceed = finishPan(traced, currentSpan, proceed);
            return proceed;
        } catch (Throwable t) {
            LOGGER.trace("[Trace: #{}] Immediate -> Error while trying to invoke underlying method: {}",
                    traced.operationName(), t.getMessage());
            if (null != t.getCause()) {
                LOGGER.trace("[Trace: #{}] Cause of underlying error: {}",
                        traced.operationName(), t.getCause().getMessage());
            }
            currentSpan = enhanceSpanFromException(traced, currentSpan, t);
            LOGGER.trace("[Trace: #{} Immediate -> Finishing span now.", traced.operationName());
            currentSpan.finish();
            LOGGER.trace("[Trace: #{}] Immediate -> Finished span with exception -> spanId={}, traceId={}",
                    traced.operationName(),
                    currentSpan.context().toSpanId(), currentSpan.context().toTraceId());
            throw t;
        }
    }

    private Span enhanceSpanFromException(Traced traced, Span currentSpan, Throwable t) {
        LOGGER.trace("[Trace: #{}] Extracting and adding tags from exception", traced.operationName());
        Span ss = currentSpan;
        ss = ss.setTag("service.call.status", "FAILED");
        LOGGER.trace("[Trace: #{}] Exception type is {}", traced.operationName(), t.getClass().getName());
        Throwable t1 = TracingCandidateExceptionExtractor.getCandidate(t);
        if (t1 != null) {
            if (t1 instanceof StatusRuntimeException) {
                GrpcStatus grpcStatus = GrpcStatus.from((StatusRuntimeException) t1);
                ss = ss.setTag("grpc.status.code.value", grpcStatus.getStatusCodeValue());
                LOGGER.trace("[Trace: #{}] Added tag {} : {}", traced.operationName(), "grpc.status.code.value", grpcStatus.getStatusCodeValue());
                ss = ss.setTag("grpc.status.code", grpcStatus.getStatusCode().name());
                LOGGER.trace("[Trace: #{}] Added tag {} : {}", traced.operationName(), "grpc.status.code", grpcStatus.getStatusCode());
                ss = ss.setTag("grpc.status.description", grpcStatus.getDescription());
                LOGGER.trace("[Trace: #{}] Added tag {} : {}", traced.operationName(), "grpc.status.description", grpcStatus.getDescription());
                ss = ss.setTag("grpc.status.error.cause", grpcStatus.getErrorCauseDescription());
                LOGGER.trace("[Trace: #{}] Added tag {} : {}", traced.operationName(), "grpc.status.error.cause", grpcStatus.getErrorCauseDescription());
                LOGGER.trace("[Trace: #{}] Span was enhanced with exception data", traced.operationName());
            }
        } else {
            LOGGER.trace("[Trace: #{}] Extractor returned null.", traced.operationName());
        }
        return ss;
    }

    @SuppressWarnings({"DuplicatedCode", "unchecked", "rawtypes"})
    private Object finishPan(Traced traced, Span currentSpan, Object result) {
        Object proceed = result;
        if (proceed instanceof CompletionStage) {
            // finish the span sometime in the future
            proceed = ((CompletionStage) proceed).whenComplete((res, t) ->
                    finishSpanInFuture(traced, currentSpan, res, ((Throwable) t)));
        } else {
            if (proceed instanceof Source) {
                // finish the span sometime in the future
                proceed = ((Source) proceed).watchTermination((Function2) ((arg1, computeDone) -> {
                    CompletionStage<Done> done = (CompletionStage<Done>) computeDone;
                    return done.whenComplete((res, t) -> finishSpanInFuture(traced, currentSpan, res, t));
                }));
            } else {
                // finish span now
                finishSpanNow(traced, currentSpan, proceed);
            }
        }
        return proceed;
    }

    private void finishSpanInFuture(Traced traced, Span currentSpan, Object proceed, Throwable t) {
        LOGGER.trace("[Trace: #{}] Future -> Finishing span in the future!", traced.operationName());
        if (t != null) {
            currentSpan.log("Error: " + t.getMessage());
            currentSpan = enhanceSpanFromException(traced, currentSpan, t);
            currentSpan.finish();
            LOGGER.trace("[Trace: #{}] Finished span with exception in future -> spanId={}, traceId={}", traced.operationName(),
                    currentSpan.context().toSpanId(), currentSpan.context().toTraceId());
        } else {
            currentSpan = enhanceSpanFromResponse(traced, currentSpan, proceed);
            currentSpan = currentSpan.setTag("service.call.status", "OK");
            currentSpan.finish();
            LOGGER.trace("[Trace: #{}] Finished span in future-> spanId={}, traceId={}", traced.operationName(),
                    currentSpan.context().toSpanId(), currentSpan.context().toTraceId());
        }
    }

    private void finishSpanNow(Traced traced, Span currentSpan, Object proceed) {
        Span ss = currentSpan;
        currentSpan = enhanceSpanFromResponse(traced, currentSpan, proceed);
        currentSpan = currentSpan.setTag("service.call.status", "OK");
        currentSpan.finish();
        LOGGER.trace("[Trace: #{}] Finished span now -> spanId={}, traceId={}", traced.operationName(),
                currentSpan.context().toSpanId(), currentSpan.context().toTraceId());
    }

    private TextMap getCarrier(Traced traced, ProceedingJoinPoint joinPoint) {
        TextMap carrier = new TextMapAdapter(new HashMap<>());
        switch (traced.headerType()) {
            case ALL:
                TextMap gCarrier = getGrpcCarrier(traced, joinPoint);
                TextMap hCarrier = getHttpCarrier(traced, joinPoint);
                for (Map.Entry<String, String> e : gCarrier) {
                    carrier.put(e.getKey(), e.getValue());
                }
                for (Map.Entry<String, String> e : hCarrier) {
                    carrier.put(e.getKey(), e.getValue());
                }
                break;
            case GRPC:
                carrier = getGrpcCarrier(traced, joinPoint);
                break;
            case HTTP:
                carrier = getHttpCarrier(traced, joinPoint);
                break;
            default:
                break;
        }
        return carrier;
    }

    private Map<String, String> getHttpRequestInfo(Traced traced, ProceedingJoinPoint joinPoint) {
        Map<String, String> tagMap = new HashMap<>();
        Object[] signatureArgs = joinPoint.getArgs();
        HttpServletRequest httpServletRequest = null;
        for (Object o : signatureArgs) {
            if (o instanceof HttpServletRequest) {
                httpServletRequest = (HttpServletRequest) o;
            }
        }
        if (null != httpServletRequest) {
            tagMap.put("http.uri", httpServletRequest.getRequestURI());
            tagMap.put("http.method", httpServletRequest.getMethod());
            tagMap.put("component", "net/http");
        }
        return tagMap;
    }

    private Map<String, String> getGrpcRequestInfo(Traced traced, ProceedingJoinPoint joinPoint) {
        Map<String, String> tagMap = new HashMap<>();
        Object[] signatureArgs = joinPoint.getArgs();
        Metadata metadata = null;
        //tagMap.put("component", "gRPC");
        for (Object o : signatureArgs) {
            if (o instanceof Metadata) {
                metadata = (Metadata) o;
            }
        }
        if (null != metadata) {
            metadata.getText("grpc-method-name").ifPresent(d -> tagMap.put("grpc.method.name", d));
            metadata.getText("grpc-method-type").ifPresent(d -> tagMap.put("grpc.method.type", d));
        }
        return tagMap;
    }

    private TextMap getGrpcCarrier(Traced traced, ProceedingJoinPoint joinPoint) {
        TextMap grpcMetadataCarrier = new TextMapAdapter(new HashMap<>());
        Object[] signatureArgs = joinPoint.getArgs();
        Metadata metadata = null;
        for (Object o : signatureArgs) {
            if (o instanceof Metadata) {
                metadata = (Metadata) o;
            }
        }

        if (null != metadata) {
            LOGGER.trace("[Trace: #{}] Extracted metadata -> {}", traced.operationName(), metadata.asList());
            //grpcMetadataCarrier = new TextMapAdapter(new HashMap<>());
            for (Pair<String, MetadataEntry> p : metadata.asList()) {
                String key = p.first();
                String value = ((StringEntry) p.second()).getValue();
                grpcMetadataCarrier.put(key, value);
            }
        }
        return grpcMetadataCarrier;
    }

    private TextMap getHttpCarrier(Traced traced, ProceedingJoinPoint joinPoint) {
        TextMap httpMetadataCarrier = new TextMapAdapter(new HashMap<>());
        Object[] signatureArgs = joinPoint.getArgs();
        HttpHeaders httpHeaders = null;
        for (Object o : signatureArgs) {
            if (o instanceof HttpHeaders) {
                httpHeaders = (HttpHeaders) o;
            }
        }

        if (null != httpHeaders) {
            LOGGER.trace("[Trace: #{}] Extracted metadata -> {}", traced.operationName(), httpHeaders.toSingleValueMap());
            for (Map.Entry<String, String> e : httpHeaders.toSingleValueMap().entrySet()) {
                httpMetadataCarrier.put(e.getKey(), e.getValue());
            }
        }
        return httpMetadataCarrier;
    }

    private Span enhanceSpanFromRequest(Traced traced, Span span, ProceedingJoinPoint joinPoint) {
        Map<String, String> tagMapHttp = getHttpRequestInfo(traced, joinPoint);
        for (Map.Entry<String, String> e : tagMapHttp.entrySet()) {
            span = span.setTag(e.getKey(), e.getValue());
        }
        Map<String, String> tagMapGrpc = getGrpcRequestInfo(traced, joinPoint);
        for (Map.Entry<String, String> e : tagMapGrpc.entrySet()) {
            span = span.setTag(e.getKey(), e.getValue());
        }

        for (TraceTag tt : traced.traceTags()) {
            span = span.setTag(tt.key(), tt.value());
        }
        return span;
    }

    private Span enhanceSpanFromResponse(Traced traced, Span currentSpan, Object proceed) {
        Span ss = currentSpan;
        if (proceed instanceof ResponseEntity) {
            ResponseEntity responseEntity = (ResponseEntity) proceed;
            ss = currentSpan.setTag("http.status_code", responseEntity.getStatusCode().value());
        }
        return ss;
    }
}
