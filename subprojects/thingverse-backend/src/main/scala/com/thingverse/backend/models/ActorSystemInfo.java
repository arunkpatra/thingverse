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

package com.thingverse.backend.models;

import akka.actor.typed.ActorSystem;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ActorSystemInfo {

    private Optional<ActorSystem<Void>> actorSystem;
    private Optional<Set<String>> roles;
    private Optional<String> address;
    /**
     * The status of this actor system.
     */
    private ActorSystemStatus status = ActorSystemStatus.UNKNOWN;

    public ActorSystemInfo(Optional<ActorSystem<Void>> actorSystem, Optional<Set<String>> roles,
                           Optional<String> address, ActorSystemStatus status) {
        this.actorSystem = actorSystem;
        this.roles = roles;
        this.address = address;
        this.status = status;
    }

    public Optional<ActorSystem<Void>> getActorSystem() {
        return actorSystem;
    }

    public void setActorSystem(Optional<ActorSystem<Void>> actorSystem) {
        this.actorSystem = actorSystem;
    }

    public Optional<Set<String>> getRoles() {
        return roles;
    }

    public void setRoles(Optional<Set<String>> roles) {
        this.roles = roles;
    }

    public Optional<String> getAddress() {
        return address;
    }

    public void setAddress(Optional<String> address) {
        this.address = address;
    }

    public ActorSystemStatus getStatus() {
        return status;
    }

    public void setStatus(ActorSystemStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ActorSystemInfo{" +
                "actorSystem=" + (actorSystem.isPresent() ? actorSystem.get().name() : "UNAVAILABLE") +
                ", roles=" + (roles.orElse(new HashSet<>())) +
                ", address=" + (address.orElse("UNAVAILABLE")) +
                ", status=" + status +
                '}';
    }
}
