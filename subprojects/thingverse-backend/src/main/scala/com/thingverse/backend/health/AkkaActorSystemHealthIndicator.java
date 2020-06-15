package com.thingverse.backend.health;

import com.thingverse.backend.models.ActorSystemInfo;
import com.thingverse.backend.models.ActorSystemStatus;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class AkkaActorSystemHealthIndicator implements HealthIndicator {

    private final ActorSystemInfo actorSystemInfo;

    public AkkaActorSystemHealthIndicator(ActorSystemInfo actorSystemInfo) {
        this.actorSystemInfo = actorSystemInfo;
    }

    @Override
    public Health health() {
        if (ActorSystemStatus.STARTED.equals(actorSystemInfo.getStatus())) {
            return Health.up().build();
        } else {
            return Health.down().withDetail("Error Code", -1).build();
        }
    }
}
