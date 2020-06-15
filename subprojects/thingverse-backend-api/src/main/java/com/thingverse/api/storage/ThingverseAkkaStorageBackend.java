package com.thingverse.api.storage;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.stream.javadsl.Source;
import com.thingverse.api.event.ThingverseEvent;
import com.thingverse.api.event.ThingverseEventProcessorStream;

import java.util.Map;

public interface ThingverseAkkaStorageBackend {

    void init(Map<String, Object> backendContext);

    /**
     * Start the backend
     *
     * @return The storage backend instance
     */
    ThingverseAkkaStorageBackend start();

    /**
     * Stop the backend
     */
    void stop();

    Source<String, NotUsed> getPersistenceIDsSource(ActorSystem<?> system);

    ThingverseEventProcessorStream<ThingverseEvent> getEventProcessorStream();
}
