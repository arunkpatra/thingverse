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
