package com.thingverse.gradle.tasks;

import com.thingverse.gradle.extensions.ThingversePluginExtension;
import org.gradle.api.tasks.TaskAction;

public class ThingverseInfoTask extends AbstractThingverseTask {
    @TaskAction
    public void info() {
        ThingversePluginExtension extension = getProject().getExtensions().findByType(ThingversePluginExtension.class);
        if (extension == null) {
            extension = new ThingversePluginExtension();
        }
        System.out.println(String.format("Welcome to %s version: %s", extension.getProductName(), getProject().getVersion()));
    }

    @Override
    public String getDescription() {
        return "Returns a helpful message from Thingverse";
    }
}
