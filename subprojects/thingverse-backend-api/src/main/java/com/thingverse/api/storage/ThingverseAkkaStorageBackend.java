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
