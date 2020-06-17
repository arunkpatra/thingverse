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

import akka.cluster.Member;

public class MemberInfo {

    private final String address;
    private final String dataCenter;
    private final String roles;
    private final String uniqueAddress;
    private final int upNumber;
    private final String status;

    public MemberInfo(Member m) {
        this.address = m.address().toString();
        this.dataCenter = m.dataCenter();
        this.roles = m.getRoles().toString();
        this.uniqueAddress = m.uniqueAddress().address().toString();
        this.upNumber = m.upNumber();
        this.status = m.status().toString();
    }

    public String getAddress() {
        return address;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public String getRoles() {
        return roles;
    }

    public String getUniqueAddress() {
        return uniqueAddress;
    }

    public int getUpNumber() {
        return upNumber;
    }

    public String getStatus() {
        return status;
    }
}
