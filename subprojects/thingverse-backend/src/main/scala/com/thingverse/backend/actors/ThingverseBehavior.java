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

package com.thingverse.backend.actors;

import akka.actor.typed.javadsl.ActorContext;
import com.typesafe.config.Config;

public interface ThingverseBehavior<C, E, S> {
    /**
     * Returns the designated thing name.
     *
     * @return The thing name.
     */
    String getThingName();

    /**
     * Returns the extracted config for the thing.
     *
     * @return The thing specific config.
     */
    Config getThingConfig();

    ActorContext<C> getActorContext();

    /**
     * Sets a timeout after which the thing will be passivated.
     *
     * @param timeOutCommand The timeout command.
     */
    void setTimeOut(C timeOutCommand);
}
