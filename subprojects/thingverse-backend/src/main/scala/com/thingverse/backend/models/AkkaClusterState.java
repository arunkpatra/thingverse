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
