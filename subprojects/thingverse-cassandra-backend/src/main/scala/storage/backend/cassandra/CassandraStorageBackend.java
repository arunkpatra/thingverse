package storage.backend.cassandra;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.PersistenceQuery;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.javadsl.Source;
import com.thingverse.api.event.ThingverseEvent;
import com.thingverse.api.event.ThingverseEventProcessorStream;
import com.thingverse.api.storage.ThingverseAkkaStorageBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.backend.cassandra.config.CassandraBackendProperties;
import storage.backend.cassandra.events.CassandraEventProcessorStream;
import storage.backend.cassandra.launcher.CassandraLauncherWrapper;

import javax.annotation.PreDestroy;
import java.util.Map;

public class CassandraStorageBackend implements ThingverseAkkaStorageBackend {
    private static Logger LOGGER = LoggerFactory.getLogger(CassandraStorageBackend.class);
    private final CassandraBackendProperties properties;
    private final ThingverseEventProcessorStream<ThingverseEvent> eventProcessorStream = new CassandraEventProcessorStream();

    public CassandraStorageBackend(CassandraBackendProperties properties) {
        LOGGER.info(">>> Configuring Cassandra Storage Backend");
        this.properties = properties;
    }

    @Override
    public synchronized void init(Map<String, Object> backendContext) {
        CassandraSession session = (CassandraSession) backendContext.get("cassandra-session");
        if (null == session) {
            throw new RuntimeException("Caller did not provide a CassandraSession object. Aborting.");
        }
        // TODO use real replication strategy in real application
        String keyspaceStmt =
                "CREATE KEYSPACE IF NOT EXISTS thingverse_keyspace \n" +
                        "WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 } \n";

        String offsetTableStmt =
                "CREATE TABLE IF NOT EXISTS thingverse_keyspace.offsetStore ( \n" +
                        "  eventProcessorId text, \n" +
                        "  tag text, \n" +
                        "  timeUuidOffset timeuuid, \n" +
                        "  PRIMARY KEY (eventProcessorId, tag) \n" +
                        ") \n";
        LOGGER.info("Creating keyspace and offset table.");
        // ok to block here, main thread
        try {
            session.executeDDL(keyspaceStmt).toCompletableFuture().get();
            session.executeDDL(offsetTableStmt).toCompletableFuture().get();
        } catch (Exception e) {
            LOGGER.error("An error occurred " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public ThingverseAkkaStorageBackend start() {
        if (!properties.isEmbedded()) {
            return this;
        }
        CassandraLauncherWrapper.start(properties);
        return this;
    }

    @Override
    public void stop() {
        if (!properties.isEmbedded()) {
            return;
        }
        try {
            LOGGER.info("Sending stop command to Cassandra daemon which is running in a forked process. " +
                    "Will not wait for the daemon to die. You should check the status of any running java processes " +
                    "using `ps aux | grep -i java` or use another way suitable for your operating system.");
            CassandraLauncherWrapper.stop();
        } catch (Throwable t) {
            // Just ignore any throwable. Can't do anything about them anyway.
        }
    }

    @Override
    public Source<String, NotUsed> getPersistenceIDsSource(ActorSystem<?> system) {
        CassandraReadJournal queries = PersistenceQuery.get(system)
                .getReadJournalFor(CassandraReadJournal.class, CassandraReadJournal.Identifier());
        return queries.currentPersistenceIds().collectType(String.class);
    }

    @Override
    public ThingverseEventProcessorStream<ThingverseEvent> getEventProcessorStream() {
        return eventProcessorStream;
    }

    @PreDestroy
    public void shutDown() {
        if (!properties.isEmbedded()) {
            return;
        }
        stop();
    }
}
