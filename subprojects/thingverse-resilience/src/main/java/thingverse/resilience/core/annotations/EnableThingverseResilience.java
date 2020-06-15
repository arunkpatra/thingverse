package thingverse.resilience.core.annotations;

import org.springframework.context.annotation.Import;
import thingverse.resilience.core.config.ThingverseResilienceImportSelector;
import thingverse.resilience.core.config.ThingverseResilienceProperties;

import java.lang.annotation.*;

/**
 * Use this annotation on any SpringBoot Application class to enable a cassandra based akka storage backend.
 * <p>
 * See {@link ThingverseResilienceProperties}.
 * <p>
 * You can provide properties to override defaults, e.g.
 * <p>
 * thingverse.storage.backend.cassandra.port=9999
 * thingverse.storage.backend.cassandra.path=target/path
 * <p>
 * Enable or disable using the thingverse.storage.backend.cassandra.enabled property.
 * </p>
 *
 * @author Arun Patra
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ThingverseResilienceImportSelector.class)
public @interface EnableThingverseResilience {

}
