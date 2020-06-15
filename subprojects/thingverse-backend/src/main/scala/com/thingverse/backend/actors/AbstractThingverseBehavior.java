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
