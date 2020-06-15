package thingverse.tracing.annotation;

import org.springframework.context.annotation.EnableAspectJAutoProxy;
import thingverse.tracing.config.ThingverseTracingImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables tracing functionality in Thingverse components.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(ThingverseTracingImportSelector.class)
public @interface EnableThingverseTracing {

}
