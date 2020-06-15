package com.thingverse.backend.services;

import com.thingverse.backend.models.AkkaClusterMemberInfo;

public interface ThingverseAkkaClusterManager {

    AkkaClusterMemberInfo getClusterMemberInfo();
}
