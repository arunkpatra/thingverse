/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package storage.backend.cassandra.events;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.eventstream.EventStream;
import akka.actor.typed.javadsl.Adapter;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.Offset;
import akka.persistence.query.PersistenceQuery;
import akka.persistence.query.TimeBasedUUID;
import akka.persistence.typed.PersistenceId;
import akka.stream.SharedKillSwitch;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSessionRegistry;
import akka.stream.javadsl.RestartSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.datastax.oss.driver.api.core.cql.Row;
import com.thingverse.api.event.ThingverseEvent;
import com.thingverse.api.event.ThingverseEventProcessorStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * General purpose event processor infrastructure. Not specific to the ShoppingCart domain.
 */
public class CassandraEventProcessorStream extends ThingverseEventProcessorStream<ThingverseEvent> {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected ActorSystem<?> system;
    protected String tag;
    private String eventProcessorId;
    private CassandraReadJournal query;
    private CassandraSession session;

    public CassandraEventProcessorStream() {
    }

    @Override
    public ThingverseEventProcessorStream<ThingverseEvent> create(ActorSystem<?> system, String eventProcessorId, String tag) {
        this.system = system;
        this.eventProcessorId = eventProcessorId;
        this.tag = tag;

        query = PersistenceQuery.get(Adapter.toClassic(system))
                .getReadJournalFor(CassandraReadJournal.class, CassandraReadJournal.Identifier());
        session = CassandraSessionRegistry.get(system).sessionFor("alpakka.cassandra");
        return this;
    }

    protected CompletionStage<Object> processEvent(ThingverseEvent event, PersistenceId persistenceId, long sequenceNr) {
        log.debug("EventProcessor({}) consumed {} from {} with seqNr {}", tag, event, persistenceId, sequenceNr);
        system.eventStream().tell(new EventStream.Publish<>(event));
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    public void runQueryStream(SharedKillSwitch killSwitch) {
        RestartSource.withBackoff(Duration.ofMillis(500), Duration.ofSeconds(20), 0.1, () ->
                Source.completionStageSource(
                        readOffset().thenApply(offset -> {
                            log.info("Starting stream for tag [{}] from offset [{}]", tag, offset);
                            return processEventsByTag(offset)
                                    // groupedWithin can be used here to improve performance by reducing number of offset writes,
                                    // with the trade-off of possibility of more duplicate events when stream is restarted
                                    .mapAsync(1, this::writeOffset);

                        })))
                .via(killSwitch.flow())
                .runWith(Sink.ignore(), system);
    }

    private Source<Offset, NotUsed> processEventsByTag(Offset offset) {
        return query
                .eventsByTag(tag, offset)
                .mapAsync(1, eventEnvelope -> processEvent((ThingverseEvent) eventEnvelope.event(),
                        PersistenceId.ofUniqueId(eventEnvelope.persistenceId()), eventEnvelope.sequenceNr())
                        .thenApply(done -> eventEnvelope.offset()));
    }

    private CompletionStage<Done> writeOffset(Offset offset) {
        if (offset instanceof TimeBasedUUID) {
            UUID uuidOffset = ((TimeBasedUUID) offset).value();
            return session.executeWrite(
                    "INSERT INTO thingverse_keyspace.offsetStore (eventProcessorId, tag, timeUuidOffset) VALUES (?, ?, ?)",
                    eventProcessorId, tag, uuidOffset);
        } else {
            throw new IllegalArgumentException("Unexpected offset type " + offset);
        }
    }


    private CompletionStage<Offset> readOffset() {
        return session.selectOne(
                "SELECT timeUuidOffset FROM thingverse_keyspace.offsetStore WHERE eventProcessorId = ? AND tag = ?",
                eventProcessorId,
                tag)
                .thenApply(this::extractOffset);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Offset extractOffset(Optional<Row> maybeRow) {
        if (maybeRow.isPresent()) {
            UUID uuid = maybeRow.get().getUuid("timeUuidOffset");
            if (uuid == null) {
                return startOffset();
            } else {
                return Offset.timeBasedUUID(uuid);
            }
        } else {
            return startOffset();
        }
    }

    // start looking from one week back if no offset was stored
    private Offset startOffset() {
        return query.timeBasedUUIDFrom(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000));
    }
}
