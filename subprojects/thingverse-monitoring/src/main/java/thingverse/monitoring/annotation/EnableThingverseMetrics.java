package thingverse.monitoring.annotation;

import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import thingverse.monitoring.config.ThingverseMetricsImportSelector;

import java.lang.annotation.*;

/**
 * This configures the metrics collection infrastructure for Thingverse.
 *
 * @author Arun Patra
 * @see MetricsCollector
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(ThingverseMetricsImportSelector.class)
public @interface EnableThingverseMetrics {
}
