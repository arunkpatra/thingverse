package com.thingverse.backend.models;

import akka.cluster.Member;

public class MemberInfo {

    private final String  address;
    private final String  dataCenter;
    private final String  roles;
    private final String  uniqueAddress;
    private final int  upNumber;
    private final String  status;

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
