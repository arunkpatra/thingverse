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
