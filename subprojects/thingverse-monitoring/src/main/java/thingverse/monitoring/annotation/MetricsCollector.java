package thingverse.monitoring.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Classes annotated with {@link MetricsCollector} will be eligible to register metrics collectors.
 *
 * @author Arun Patra
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface MetricsCollector {
}
