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

package com.thingverse.backend.events;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ClusterShardingSettings;
import akka.cluster.sharding.typed.ShardedDaemonProcessSettings;
import akka.cluster.sharding.typed.javadsl.ShardedDaemonProcess;
import akka.stream.KillSwitches;
import akka.stream.SharedKillSwitch;
import com.thingverse.api.event.ThingverseEventProcessorStream;

import java.util.Optional;
import java.util.function.Function;

/**
 * General purpose event processor infrastructure. Not specific to the ShoppingCart domain.
 */
public class EventProcessor {

    public static <Event> void init(
            ActorSystem<?> system,
            EventProcessorSettings settings,
            Function<String, ThingverseEventProcessorStream<Event>> eventProcessorStream) {

        ShardedDaemonProcessSettings shardedDaemonSettings =
                ShardedDaemonProcessSettings.create(system)
                        .withKeepAliveInterval(settings.keepAliveInterval)
                        .withShardingSettings(ClusterShardingSettings.create(system).withRole("read-model"));

        ShardedDaemonProcess.get(system)
                .init(Void.class, "event-processors-" + settings.id, settings.parallelism,
                        i -> EventProcessor.create(eventProcessorStream.apply(settings.tagPrefix + "-" + i)),
                        shardedDaemonSettings, Optional.empty());
    }

    public static Behavior<Void> create(ThingverseEventProcessorStream<?> eventProcessorStream) {
        return Behaviors.setup(context -> {
            SharedKillSwitch killSwitch = KillSwitches.shared("eventProcessorSwitch");
            eventProcessorStream.runQueryStream(killSwitch);
            return Behaviors.receive(Void.class)
                    .onSignal(PostStop.class, sig -> {
                        killSwitch.shutdown();
                        return Behaviors.same();
                    })
                    .build();
        });
    }
}
