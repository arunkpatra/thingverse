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

import akka.actor.typed.BackoffSupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public abstract class AbstractThingverseBehavior<C, E, S> extends EventSourcedBehaviorWithEnforcedReplies<C, E, S>
        implements ThingverseBehavior<C, E, S> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractThingverseBehavior.class);
    private static final Config defaultConfig =
            ConfigFactory.systemProperties()
                    .withOnlyPath("thingverse")
                    .withFallback(ConfigFactory.parseResources("default-thing-props.conf"))
                    .getConfig("thingverse.things");
    private final Config cachedConfig;
    private final ActorContext<C> actorContext;

    public AbstractThingverseBehavior(ActorContext<C> actorContext, PersistenceId persistenceId,
                                      BackoffSupervisorStrategy backoffSupervisorStrategy) {
        super(persistenceId, backoffSupervisorStrategy);
        this.actorContext = actorContext;
        this.cachedConfig = getThingConfigInternal();
    }

    /**
     * By default, the simple class name is used. Sub-classes would usually prefer to override this.
     *
     * @return The thing name.
     */
    @Override
    public abstract String getThingName();

    @Override
    public Config getThingConfig() {
        return this.cachedConfig;
    }

    public ActorContext<C> getActorContext() {
        return actorContext;
    }

    public void setTimeOut(C timeOutCommand) {
        ActorContext<C> ctx = getActorContext();
        LOGGER.debug("Config for thing {} is {}", getThingName(), getThingConfig());
        if ("off".equalsIgnoreCase(getThingConfig().getString("thing-timeout-duration"))) {
            ctx.cancelReceiveTimeout();
            return;
        }
        Duration timeOutDuration = getThingConfig().getDuration("thing-timeout-duration");
        ctx.setReceiveTimeout(timeOutDuration, timeOutCommand);
    }

    private Config getThingConfigInternal() {
        String remoteThingPath = "thingverse.things.".concat(getThingName());
        Config c = getActorContext().getSystem().settings().config(); // this has some defaults for thingverse.* properties
        if (c.hasPathOrNull(remoteThingPath) && !c.getIsNull(remoteThingPath)) {
            return c.getConfig(remoteThingPath).withFallback(defaultConfig);
        } else {
            return defaultConfig;
        }
    }
}
