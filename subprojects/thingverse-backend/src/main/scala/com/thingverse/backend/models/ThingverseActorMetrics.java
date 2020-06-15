package com.thingverse.backend.models;

import com.thingverse.api.serialization.CborSerializable;

public class ThingverseActorMetrics implements CborSerializable {

    private Long totalActiveThings;
    private Long totalMessagesReceived;
    private Long averageMessageAge;

    public ThingverseActorMetrics(Long totalActiveThings, Long totalMessagesReceived, Long averageMessageAge) {
        this.totalActiveThings = totalActiveThings;
        this.totalMessagesReceived = totalMessagesReceived;
        this.averageMessageAge = averageMessageAge;
    }

    public Long getTotalActiveThings() {
        return totalActiveThings;
    }

    public Long getTotalMessagesReceived() {
        return totalMessagesReceived;
    }

    public Long getAverageMessageAge() {
        return averageMessageAge;
    }

    @Override
    public String toString() {
        return "ThingverseActorMetrics{" +
                "totalActiveThings=" + totalActiveThings +
                ", totalMessagesReceived=" + totalMessagesReceived +
                ", averageMessageAge=" + averageMessageAge +
                '}';
    }
}
