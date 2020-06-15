package com.thingverse.backend.services;

import akka.actor.typed.ActorSystem;

public interface ClusterBootStrapService {

    void startBootstrapProcess(ActorSystem<Void> system);

}
