package com.thingverse.gradle.tasks;

import org.gradle.api.DefaultTask;

public abstract class AbstractThingverseTask extends DefaultTask {
    @Override
    public String getGroup() {
        return "thingverse";
    }
}
