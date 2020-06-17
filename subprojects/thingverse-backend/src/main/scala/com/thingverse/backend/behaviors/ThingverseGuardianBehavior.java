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

package com.thingverse.backend.behaviors;

import akka.actor.Props;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.typed.Cluster;
import akka.japi.Creator;
import com.thingverse.api.command.ThingverseCommand;
import com.thingverse.api.event.ThingverseEvent;
import com.thingverse.api.event.ThingverseEventProcessorStream;
import com.thingverse.backend.actors.MetricsCollector;
import com.thingverse.backend.actors.RemoteThing;
import com.thingverse.backend.events.EventProcessor;
import com.thingverse.backend.events.EventProcessorSettings;
import com.thingverse.backend.metrics.listener.ThingverseClusterNodeMetricsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThingverseGuardianBehavior {

    private final static Logger LOGGER = LoggerFactory.getLogger(ThingverseGuardianBehavior.class);

    private static Creator<ThingverseClusterNodeMetricsListener> metricsListenerCreator = ThingverseClusterNodeMetricsListener::new;

    public static Props metricsListenerProps() {
        return Props.create(ThingverseClusterNodeMetricsListener.class, metricsListenerCreator);
    }

    public static Behavior<Void> create(ThingverseEventProcessorStream<ThingverseEvent> eventProcessorStream) {
        return Behaviors.setup(context -> {
            ActorSystem<?> system = context.getSystem();
            EventProcessorSettings settings = EventProcessorSettings.create(system);

            // Cluster Node Metrics Listener
            context.classicActorContext().actorOf(metricsListenerProps(), "ThingverseClusterNodeMetricsListener");

            // Establishing a Metrics collector
            ActorRef<ShardingEnvelope<MetricsCollector.Command>> metricsCollectorRef = MetricsCollector.init(system);
            LOGGER.info("Initialized metrics collector : {}", metricsCollectorRef.path());

            // context.watch(RemoteThing.init(system, settings));
            ActorRef<ShardingEnvelope<ThingverseCommand>> remoteThingRef = RemoteThing.init(system, settings);
            LOGGER.info("Initialized RemoteThing : {}", remoteThingRef.path());

            if (Cluster.get(system).selfMember().hasRole("read-model")) {
                EventProcessor.init(
                        system,
                        settings,
                        tag -> (eventProcessorStream.create(system, settings.id, tag)));

            }
//            return Behaviors.receive(Void.class)
//                    .onSignal(Terminated.class, sig -> {
//                        LOGGER.info("Received terminated command from {}", sig.ref().narrow().path());
//                        return Behaviors.same();
//                    })
//                    .build();
            return Behaviors.empty();
        });
    }
}
