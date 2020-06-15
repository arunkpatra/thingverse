package thingverse.tracing.annotation;


import java.lang.annotation.*;

/**
 * A method annotated with this annotation will be traced if tracing is enabled.
 *
 * @author Arun Patra
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Traced {

    /**
     * The Operation name.
     *
     * @return operation name.
     */
    String operationName();

    /**
     * The type of headers to be extracted. Applicable only when a propagated span context
     * is to be utilized.
     *
     * @return The header type.
     */
    HeaderType headerType() default HeaderType.ALL;

    /**
     * Indicates how a span is to be created. See {@link SpanType}. The default is {@link SpanType#PROPAGATED}.
     *
     * @return The span type to be created.
     */
    SpanType spanType() default SpanType.PROPAGATED;

    /**
     * The trace tags to be added.
     *
     * @return The tags to be added.
     */
    TraceTag[] traceTags() default {};

    enum HeaderType {
        /**
         * HTTP headers are to be propagated.
         */
        HTTP,

        /**
         * gRPC headers are to be propagated.
         */
        GRPC,

        /**
         * Both HTTP and gRPC headers will be tried.
         */
        ALL
    }

    enum SpanType {
        /**
         * Start a new span
         */
        NEW,

        /**
         * Create span from a locally(in same process) existing parent span.
         */
        EXISTING,

        /**
         * Create span from a propagated(from another process) parent span.
         */
        PROPAGATED
    }
}
