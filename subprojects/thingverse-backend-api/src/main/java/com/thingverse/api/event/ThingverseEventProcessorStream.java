package com.thingverse.api.event;

import akka.actor.typed.ActorSystem;
import akka.stream.SharedKillSwitch;

public abstract class ThingverseEventProcessorStream<Event> {

    public abstract void runQueryStream(SharedKillSwitch killSwitch);

    public abstract ThingverseEventProcessorStream<Event> create(ActorSystem<?> system, String eventProcessorId, String tag);
}
