package com.thingverse.monitoring.test.model;

public class MetricData {

    private final long count;
    private final long messageCount;

    public MetricData(long count, long messageCount) {
        this.count = count;
        this.messageCount = messageCount;
    }

    public long getCount() {
        return count;
    }

    public long getMessageCount() {
        return messageCount;
    }
}
