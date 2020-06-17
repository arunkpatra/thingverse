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
