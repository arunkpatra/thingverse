package com.thingverse.common.env.health;

import org.springframework.boot.context.event.ApplicationReadyEvent;

public interface EnvironmentHealthListener {

    void handleApplicationReadyEvent(ApplicationReadyEvent appReadyEvent);
}
