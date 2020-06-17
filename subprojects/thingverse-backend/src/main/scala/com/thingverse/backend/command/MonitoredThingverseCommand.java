package com.thingverse.backend.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.thingverse.api.command.ThingverseCommand;
import com.thingverse.backend.actors.RemoteThing;

import java.time.Duration;
import java.time.Instant;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RemoteThing.ClearThingCommand.class, name = "ClearThingCommand"),
        @JsonSubTypes.Type(value = RemoteThing.Get.class, name = "Get"),
        @JsonSubTypes.Type(value = RemoteThing.GetPassivationSummary.class, name = "GetPassivationSummary"),
        @JsonSubTypes.Type(value = RemoteThing.Ping.class, name = "Ping"),
        @JsonSubTypes.Type(value = RemoteThing.UpdateThingCommand.class, name = "UpdateThingCommand"),
        @JsonSubTypes.Type(value = RemoteThing.CreateThingCommand.class, name = "CreateThingCommand")
})
public abstract class MonitoredThingverseCommand implements ThingverseCommand {
    private Instant creationTime = Instant.now();
    private Instant deliveredToActorAt = Instant.now();

    public Instant getCreationTime() {
        return creationTime;
    }

    public Instant getDeliveredToActorAt() {
        return deliveredToActorAt;
    }

    public void setDeliveredToActorAt(Instant deliveredToActorAt) {
        this.deliveredToActorAt = deliveredToActorAt;
    }

    /**
     * Get message age in micro seconds
     *
     * @return The message age.
     */
    public Long getMessageAge() {
        Duration between = Duration.between(creationTime, deliveredToActorAt);
        return between.toNanos() / 1000L; // micros
    }
}
