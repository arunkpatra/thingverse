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

package com.thingverse.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "GetBackendClusterStatusResponse", description = "Backend Cluster state response")
public class GetBackendClusterStatusResponse {

    @ApiModelProperty(value = "Indicates if all cluster members are up")
    private final Boolean allMembersUp;

    @ApiModelProperty(value = "Total number of nodes in the backend cluster.")
    private final int totalNodeCount;

    @ApiModelProperty(value = "Total number of nodes with read-model role.")
    private final long readNodeCount;

    @ApiModelProperty(value = "Total number of nodes with read-model role.")
    private final long writeNodeCount;

    @JsonCreator
    public GetBackendClusterStatusResponse(Boolean allMembersUp, int totalNodeCount, long readNodeCount, long writeNodeCount) {
        this.allMembersUp = allMembersUp;
        this.totalNodeCount = totalNodeCount;
        this.readNodeCount = readNodeCount;
        this.writeNodeCount = writeNodeCount;
    }

    public Boolean getAllMembersUp() {
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
        return "GetBackendClusterStatusResponse{" +
                "allMembersUp=" + allMembersUp +
                ", totalNodeCount=" + totalNodeCount +
                ", readNodeCount=" + readNodeCount +
                ", writeNodeCount=" + writeNodeCount +
                '}';
    }
}
