package storage.backend.cassandra.config;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSessionRegistry;
import com.thingverse.api.storage.ThingverseAkkaStorageBackend;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import storage.backend.cassandra.CassandraStorageBackend;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(CassandraBackendProperties.class)
@ConditionalOnProperty(prefix = "thingverse.storage.backend.cassandra", name = {"enabled"}, matchIfMissing = true)
public class CassandraBackendConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraBackendConfiguration.class);

    private final CassandraBackendProperties properties;

    public CassandraBackendConfiguration(CassandraBackendProperties properties) {
        this.properties = properties;
    }

    /**
     * Configure Cassandra Storage Backend.
     *
     * @return ThingverseAkkaStorageBackend instance.
     */
    @Bean
    @ConditionalOnMissingBean(ThingverseAkkaStorageBackend.class)
    public ThingverseAkkaStorageBackend thingverseAkkaStorageBackend() {
        ThingverseAkkaStorageBackend sb = (new CassandraStorageBackend(properties)).start();
        // We pre-create this for embedded cassandra db. When its not an embedded db, then,
        // the tables need to be created as part of the startup cycle of the backend.
        // TODO: Use a property driven approach to create the table and keyspace. In PROD, disable this and
        // enforce that these resources are created externally beforehand.
        if (properties.isEnabled() && properties.isEmbedded()) {
            ActorSystem<Void> actorSystem =
                    ActorSystem.create(Behaviors.empty(), "storage-backend-actor-system", ConfigFactory.load());
            Map<String, Object> backendContext = new HashMap<>();
            CassandraSession session =
                    CassandraSessionRegistry.get(actorSystem).sessionFor("alpakka.cassandra");
            backendContext.put("cassandra-session", session);
            sb.init(backendContext);
            // We don't need this actorSystem anymore.
            actorSystem.terminate();
        }
        return sb;
    }
}
