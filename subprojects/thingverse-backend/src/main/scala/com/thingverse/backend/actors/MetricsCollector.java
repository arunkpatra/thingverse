package com.thingverse.backend.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.thingverse.backend.command.MonitoredThingverseCommand;
import com.thingverse.backend.models.ThingverseActorMetrics;
import com.thingverse.api.serialization.CborSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsCollector extends AbstractBehavior<MetricsCollector.Command> {

    private final static Logger LOGGER = LoggerFactory.getLogger(MetricsCollector.class);
    public static EntityTypeKey<Command> ENTITY_TYPE_KEY =
            EntityTypeKey.create(Command.class, "MetricsCollector");
    public static String METRICS_COLLECTOR_ENTITY_ID = "metrics-collector-entity-singleton-id";
    private final ActorRef<ClusterSharding.ShardCommand> shard;
    private final String entityId;
    private long averageMessageAge = 0; // micros
    private long activeThingCount = 0;
    private long totalMessagesReceived = 0;

    private MetricsCollector(
            ActorContext<Command> context,
            ActorRef<ClusterSharding.ShardCommand> shard,
            String entityId) {
        super(context);
        this.shard = shard;
        this.entityId = entityId;
    }

    public static ActorRef<ShardingEnvelope<Command>> init(ActorSystem<?> system) {
        return ClusterSharding.get(system).init(Entity.of(ENTITY_TYPE_KEY, ctx ->
                create(ctx.getShard(), ctx.getEntityId())
        ).withRole("write-model"));
    }

    public static Behavior<Command> create(ActorRef<ClusterSharding.ShardCommand> shard, String entityId) {
        LOGGER.info("Creating MetricsCollector actor...");
        return Behaviors.setup(ctx -> {
                    // TODO: See if this is needed after all, now that the
                    // TODO: akka.cluster.sharding.passivate-idle-entity-after = off has been provided in config
                    ctx.cancelReceiveTimeout();
                    return new MetricsCollector(ctx, shard, entityId);
                }
        );
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(IncrementCommand.class, this::onIncrement)
                .onMessage(DecrementCommand.class, this::onDecrement)
                .onMessage(MessageReceivedCommand.class, this::onMessageReceived)
                .onMessage(GetActorMetrics.class, this::onGetActorMetrics)
                .build();
    }

    private Behavior<Command> onGetActorMetrics(GetActorMetrics msg) {
        ThingverseActorMetrics metrics =
                new ThingverseActorMetrics(activeThingCount, totalMessagesReceived, averageMessageAge);
        //LOGGER.info("Got new Request for metrics. Sending result: {}", metrics);
        msg.replyTo.tell(metrics);
        return this;
    }

    private Behavior<Command> onIncrement(IncrementCommand msg) {
        activeThingCount++;
        return this;
    }

    private Behavior<Command> onDecrement(DecrementCommand msg) {
        if (activeThingCount > 0) {
            activeThingCount--;
        }
        return this;
    }

    private Behavior<Command> onMessageReceived(MessageReceivedCommand message) {
        totalMessagesReceived++;
        this.averageMessageAge = ((this.averageMessageAge * (totalMessagesReceived - 1)) +
                message.message.getMessageAge())
                / totalMessagesReceived;
        return this;
    }

    public interface Command extends CborSerializable {
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class IncrementCommand implements Command {
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class DecrementCommand implements Command {
    }

    public static class MessageReceivedCommand implements Command {
        private final MonitoredThingverseCommand message;

        @JsonCreator
        public MessageReceivedCommand(MonitoredThingverseCommand message) {
            this.message = message;
        }
    }

    public static class GetActorMetrics implements Command {
        private final ActorRef<ThingverseActorMetrics> replyTo;

        @JsonCreator
        public GetActorMetrics(ActorRef<ThingverseActorMetrics> replyTo) {
            this.replyTo = replyTo;
        }
    }
}
