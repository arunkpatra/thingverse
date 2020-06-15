package com.thingverse.gradle.plugins;

import com.thingverse.gradle.extensions.ThingversePluginExtension;
import com.thingverse.gradle.tasks.ThingverseInfoTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ThingversePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("thingverseSetting", ThingversePluginExtension.class);
        project.getTasks().create("thingverseInfo", ThingverseInfoTask.class);
    }
}
