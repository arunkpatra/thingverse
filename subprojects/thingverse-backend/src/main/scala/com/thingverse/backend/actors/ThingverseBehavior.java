package com.thingverse.backend.actors;

import akka.actor.typed.javadsl.ActorContext;
import com.typesafe.config.Config;

public interface ThingverseBehavior<C, E, S> {
    /**
     * Returns the designated thing name.
     *
     * @return The thing name.
     */
    String getThingName();

    /**
     * Returns the extracted config for the thing.
     *
     * @return The thing specific config.
     */
    Config getThingConfig();

    ActorContext<C> getActorContext();

    /**
     * Sets a timeout after which the thing will be passivated.
     *
     * @param timeOutCommand The timeout command.
     */
    void setTimeOut(C timeOutCommand);
}
