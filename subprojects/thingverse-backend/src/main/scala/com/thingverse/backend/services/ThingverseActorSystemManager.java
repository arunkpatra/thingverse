package com.thingverse.backend.services;

import com.thingverse.backend.models.ActorSystemInfo;
import com.thingverse.backend.models.ActorSystemInfoFormatted;

public interface ThingverseActorSystemManager {

    /**
     * Create the actor system for the backend.
     *
     * @return ActorSystemInfo
     */
    ActorSystemInfo createActorSystem();

    /**
     * Returns information about the actor system owned by this bean.
     *
     * @return ActorSystemInfo
     */
    ActorSystemInfo getActorSystemInfo();

    /**
     * Terminates the Thingverse Backend ActorSystem managed by this bean
     *
     * @return ActorSystemInfoFormatted
     */
    ActorSystemInfoFormatted terminateActorSystem();

    /**
     * Get the current hierarchy of actors in the th Akka ActorSystem managed by this bean.
     *
     * @return The actor hierarchy.
     */
    String getActorSystemTree();

    String getActorSystemSettings();
}
