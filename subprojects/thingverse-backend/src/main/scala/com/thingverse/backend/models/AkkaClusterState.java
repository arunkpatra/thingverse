package com.thingverse.backend.models;

import com.thingverse.api.serialization.CborSerializable;

public class AkkaClusterState implements CborSerializable {

    private final boolean allMembersUp;
    private final int totalNodeCount;
    private final long readNodeCount;
    private final long writeNodeCount;

    public AkkaClusterState(boolean allMembersUp, int totalNodeCount, long readNodeCount, long writeNodeCount) {
        this.allMembersUp = allMembersUp;
        this.totalNodeCount = totalNodeCount;
        this.readNodeCount = readNodeCount;
        this.writeNodeCount = writeNodeCount;
    }

    public boolean isAllMembersUp() {
        return allMembersUp;
    }

    public int getTotalNodeCount() {
        return totalNodeCount;
    }

    public long getReadNodeCount() {
        return readNodeCount;
    }

    public long getWriteNodeCount() {
        return writeNodeCount;
    }

    @Override
    public String toString() {
        return "AkkaClusterState{" +
                "allMembersUp=" + allMembersUp +
                ", totalNodeCount=" + totalNodeCount +
                ", readNodeCount=" + readNodeCount +
                ", writeNodeCount=" + writeNodeCount +
                '}';
    }
}
