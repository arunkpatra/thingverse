package thingverse.tracing.config;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps a {@link io.opentracing.Tracer} implementation. Currently we use a Jaeger implementation
 * of the tracer.
 */
public class ThingverseTracer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseTracer.class);
    public final boolean enabled;
    public final io.opentracing.Tracer tracer;

    public ThingverseTracer(boolean enabled, Tracer tracer) {
        this.enabled = enabled;
        this.tracer = tracer;
    }

    public Map<String, String> extractGrpcMetadataFromExistingSpan() {
        Map<String, String> metadataMap = new HashMap<>();
        if (enabled) {
            try {
                Span ss = tracer.activeSpan();
                TextMap grpcMetadataCarrier = new TextMapAdapter(new HashMap<>());
                tracer.inject(ss.context(), Format.Builtin.HTTP_HEADERS, grpcMetadataCarrier);
                LOGGER.trace("Injected carrier data successfully.");
                for (Map.Entry<String, String> e : grpcMetadataCarrier) {
                    metadataMap.put(e.getKey(), e.getValue());
                }
            } catch (Throwable t) {
                LOGGER.trace("Failed to inject carrier data. Hence, headers will not be propagated. Error: {}", t.getMessage());
            }
        }
        return metadataMap;
    }
}
