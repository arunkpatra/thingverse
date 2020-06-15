package thingverse.grpc.client.annotation;

import org.springframework.context.annotation.Import;
import thingverse.grpc.client.config.ThingverseGrpcClientImportSelector;
import thingverse.grpc.client.config.ThingverseGrpcClientProperties;

import java.lang.annotation.*;

/**
 * Use this annotation on any SpringBoot Application class to enable a cassandra based akka storage backend.
 * <p>
 * See {@link ThingverseGrpcClientProperties}.
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
@Import(ThingverseGrpcClientImportSelector.class)
public @interface EnableThingverseGrpcClient {

}
