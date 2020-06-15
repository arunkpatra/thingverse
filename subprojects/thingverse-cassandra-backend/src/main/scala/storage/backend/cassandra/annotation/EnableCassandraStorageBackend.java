package storage.backend.cassandra.annotation;

import org.springframework.context.annotation.Import;
import storage.backend.cassandra.config.CassandraBackendImportSelector;

import java.lang.annotation.*;

/**
 * Use this annotation on any SpringBoot Application class to enable a cassandra based akka storage backend.
 * <p>
 * See {@link storage.backend.cassandra.config.CassandraBackendProperties}.
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
//@AutoConfigureOrder(value = Ordered.HIGHEST_PRECEDENCE + 100)
@Import(CassandraBackendImportSelector.class)
public @interface EnableCassandraStorageBackend {

}
