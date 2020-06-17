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

package com.thingverse.backend.services.impl;

import akka.actor.typed.ActorSystem;
import akka.cluster.typed.Cluster;
import com.thingverse.backend.models.AkkaClusterMemberInfo;
import com.thingverse.backend.models.MemberInfo;
import com.thingverse.backend.services.ThingverseAkkaClusterManager;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.ArrayList;
import java.util.List;

@ManagedResource(objectName = "thingverse:name=ThingverseAkkaClusterManager", description = "Thingverse Akka Cluster members")
public class ThingverseAkkaClusterManagerImpl implements ThingverseAkkaClusterManager {

    private final ActorSystem<Void> actorSystem;

    public ThingverseAkkaClusterManagerImpl(ActorSystem<Void> actorSystem) {
        this.actorSystem = actorSystem;
    }

    @Override
    @ManagedOperation(description = "Akka Cluster member information.")
    public AkkaClusterMemberInfo getClusterMemberInfo() {
        List<MemberInfo> memberInfoList = new ArrayList<>();
        Cluster.get(actorSystem).state().getMembers().forEach(m -> {
            memberInfoList.add(new MemberInfo(m));
        });
        return new AkkaClusterMemberInfo(new MemberInfo(Cluster.get(actorSystem).selfMember()), memberInfoList);
    }
}
