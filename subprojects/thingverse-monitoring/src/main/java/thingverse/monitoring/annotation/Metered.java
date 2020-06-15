package thingverse.monitoring.annotation;

import io.micrometer.core.instrument.Meter;

import java.lang.annotation.*;

/**
 * A method annotated with this annotation will be eligible for metering.
 *
 * @author Arun Patra
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Metered {

    /**
     * The metric name.
     *
     * @return metric name.
     */
    String metricName();

    /**
     * Optional tags.
     *
     * @return tags
     */
    String[] tags() default {};

    Class<? extends Meter> type();
}
