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
