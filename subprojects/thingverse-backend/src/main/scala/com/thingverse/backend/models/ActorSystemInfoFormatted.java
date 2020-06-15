package com.thingverse.backend.models;


public class ActorSystemInfoFormatted {

    private final String actorSystemName;
    private final String roles;
    private final String address;
    private final ActorSystemStatus status;
    private final String upTime;
    private final String startTime;

    public ActorSystemInfoFormatted(String actorSystemName, String roles, String address, ActorSystemStatus status,
                                    String upTime, String startTime) {
        this.actorSystemName = actorSystemName;
        this.roles = roles;
        this.address = address;
        this.status = status;
        this.upTime = upTime;
        this.startTime = startTime;
    }

    public String getActorSystemName() {
        return actorSystemName;
    }

    public String getRoles() {
        return roles;
    }

    public String getAddress() {
        return address;
    }

    public ActorSystemStatus getStatus() {
        return status;
    }

    public String getUpTime() {
        return upTime;
    }

    public String getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "ActorSystemInfoFormatted{" +
                "actorSystemName='" + actorSystemName + '\'' +
                ", roles='" + roles + '\'' +
                ", address='" + address + '\'' +
                ", status=" + status +
                ", upTime='" + upTime + '\'' +
                ", startTime='" + startTime + '\'' +
                '}';
    }
}
