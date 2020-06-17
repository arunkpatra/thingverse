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

@ApiModel(value = "GetActorMetricsResponse", description = "Actor metrics response")
public class GetActorMetricsResponse {

    @ApiModelProperty(value = "Total number of active things in the Akka cluster.")
    private Long activeThingCount = 0L;

    @ApiModelProperty(value = "Total messages received by the Akka cluster.")
    private Long totalMessagesReceived = 0L;

    @ApiModelProperty(value = "Average message age in micro seconds.")
    private Long averageMessageAge = 0L;

    @JsonCreator
    public GetActorMetricsResponse(Long activeThingCount, Long totalMessagesReceived, Long averageMessageAge) {
        this.activeThingCount = activeThingCount;
        this.totalMessagesReceived = totalMessagesReceived;
        this.averageMessageAge = averageMessageAge;
    }

    public Long getAverageMessageAge() {
        return averageMessageAge;
    }

    public Long getActiveThingCount() {
        return activeThingCount;
    }

    public Long getTotalMessagesReceived() {
        return totalMessagesReceived;
    }

    @Override
    public String toString() {
        return "GetActorMetricsResponse{" +
                "activeThingCount=" + activeThingCount +
                ", totalMessagesReceived=" + totalMessagesReceived +
                ", averageMessageAge=" + averageMessageAge +
                '}';
    }
}
