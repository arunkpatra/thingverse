package com.thingverse.backend.models;

public class AkkaClusterMemberInfo {

    private final MemberInfo self;
    private final Iterable<MemberInfo> members;

    public AkkaClusterMemberInfo(MemberInfo self, Iterable<MemberInfo> members) {
        this.self = self;
        this.members = members;
    }

    public MemberInfo getSelf() {
        return self;
    }

    public Iterable<MemberInfo> getMembers() {
        return members;
    }
}
