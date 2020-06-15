package com.thingverse.backend.actors;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.ShardingEnvelope;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.*;
import akka.persistence.typed.javadsl.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.thingverse.backend.command.MonitoredThingverseCommand;
import com.thingverse.backend.events.EventProcessorSettings;
import com.thingverse.backend.interceptors.ThingverseBehaviourInterceptor;
import com.thingverse.api.command.ThingverseCommand;
import com.thingverse.api.event.ThingverseEvent;
import com.thingverse.api.serialization.CborSerializable;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.reflect.ClassTag;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RemoteThing extends AbstractThingverseBehavior<ThingverseCommand, ThingverseEvent, RemoteThing.State> {
    private static final String THING_NAME = "remote-thing";
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteThing.class);
    public static EntityTypeKey<ThingverseCommand> ENTITY_TYPE_KEY = EntityTypeKey.create(ThingverseCommand.class,
            "RemoteThing");
    private final String thingID;
    private final Set<String> eventProcessorTags;
    private final RemoteThingCommandHandlers remoteThingCommandHandlers = new RemoteThingCommandHandlers();
    private final RemoteThingSignalHandlers remoteThingSignalHandlers = new RemoteThingSignalHandlers();
    private final ActorRef<ClusterSharding.ShardCommand> shard;

    private RemoteThing(ActorContext<ThingverseCommand> actorContext,
                        ActorRef<ClusterSharding.ShardCommand> shard,
                        String thingID,
                        Set<String> eventProcessorTags) {
        super(actorContext, PersistenceId.of(ENTITY_TYPE_KEY.name(), thingID),
                SupervisorStrategy.restartWithBackoff(Duration.ofMillis(200), Duration.ofSeconds(5), 0.1));
        this.thingID = thingID;
        this.eventProcessorTags = eventProcessorTags;
        this.shard = shard;
        // setting timeout
        setTimeOut(Idle.INSTANCE);

        LOGGER.debug("ThingID: {}, Shard Path: {}, Actor Path: {}", thingID, shard.path().address().toString(),
                actorContext.getSelf().path());
    }

    public static ActorRef<ShardingEnvelope<ThingverseCommand>> init(ActorSystem<?> system, EventProcessorSettings eventProcessorSettings) {
        return ClusterSharding.get(system).init(Entity.of(ENTITY_TYPE_KEY, entityContext -> {
            int n = Math.abs(entityContext.getEntityId().hashCode() % eventProcessorSettings.parallelism);
            String eventProcessorTag = eventProcessorSettings.tagPrefix + "-" + n;
            return create(entityContext.getShard(), entityContext.getEntityId(),
                    Collections.singleton(eventProcessorTag));
        }).withStopMessage(new GoodBye()).withRole("write-model"));
    }

    public static Behavior<ThingverseCommand> create(ActorRef<ClusterSharding.ShardCommand> shard,
                                                     String entityId,
                                                     Set<String> eventProcessorTags) {
        return Behaviors.setup(ctx ->
                ThingverseBehaviourInterceptor.apply(
                        new RemoteThingInterceptor(),
                        getActualBehaviour(shard, entityId, eventProcessorTags),
                        ClassTag.apply(ThingverseCommand.class))
        );
    }

    private static Behavior<ThingverseCommand> getActualBehaviour(ActorRef<ClusterSharding.ShardCommand> shard,
                                                                  String entityId,
                                                                  Set<String> eventProcessorTags) {
        return Behaviors.setup(ctx -> (new RemoteThing(ctx, shard, entityId, eventProcessorTags)));
    }

    @Override
    public String getThingName() {
        return THING_NAME;
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandlerWithReply<ThingverseCommand, ThingverseEvent, State> commandHandler() {
        CommandHandlerWithReplyBuilder<ThingverseCommand, ThingverseEvent, State> b =
                newCommandHandlerWithReplyBuilder();

        b.forAnyState()
                .onCommand(CreateThingCommand.class, remoteThingCommandHandlers::onCreateThing)
                .onCommand(UpdateThingCommand.class, remoteThingCommandHandlers::onUpdateThing)
                .onCommand(GetPassivationSummary.class, remoteThingCommandHandlers::onShutdownThing)
                .onCommand(ClearThingCommand.class, remoteThingCommandHandlers::onClearThing)
                .onCommand(GoodBye.class, remoteThingCommandHandlers::onGoodBye)
                .onCommand(Idle.class, remoteThingCommandHandlers::onIdle)
                .onCommand(Ping.class, remoteThingCommandHandlers::onPing)
                .onCommand(Get.class, this::onGet);

        return b.build();
    }

    private ReplyEffect<ThingverseEvent, State> onGet(State state, Get cmd) {
        return Effect().reply(cmd.replyTo, state.toSummary());
    }

    @Override
    public EventHandler<State, ThingverseEvent> eventHandler() {
        EventHandlerBuilder<State, ThingverseEvent> b = newEventHandlerBuilder();
        return b.forAnyState()
                .onEvent(ThingCreated.class, (state, event) -> state.createThing(event.attributes))
                .onEvent(ThingUpdated.class, (state, event) -> state.updateThing(event.attributes))
                .onEvent(ThingCleared.class, (state, event) -> state.clearThing())
                .build();
    }

    @Override
    public Set<String> tagsFor(ThingverseEvent event) {
        return eventProcessorTags;
    }

    @Override
    public RetentionCriteria retentionCriteria() {
        // enable snapshotting
        Config c = getThingConfig();
        int maxEvents = c.getInt("snapshot-after-events");
        int maxSnapshots = c.getInt("max-snapshots");
        LOGGER.debug("Retention criteria set for {} as numberOfEvents = {} and keepNSnapshots = {}",
                this.getClass().getSimpleName(), maxEvents, maxSnapshots);
        return RetentionCriteria.snapshotEvery(maxEvents, maxSnapshots).withDeleteEventsOnSnapshot();
    }

    @Override
    public SignalHandler<State> signalHandler() {
        return newSignalHandlerBuilder()
                .onSignal(RecoveryFailed.class, remoteThingSignalHandlers::onRecoveryFailed)
                .onSignal(SnapshotFailed.class, remoteThingSignalHandlers::onSnapshotFailed)
                .onSignal(DeleteSnapshotsFailed.class, remoteThingSignalHandlers::onDeleteSnapshotsFailed)
                .onSignal(DeleteEventsFailed.class, remoteThingSignalHandlers::onDeleteEventsFailed)
                .build();
    }

    public enum Idle implements ThingverseCommand {
        INSTANCE
    }

    public interface Confirmation extends CborSerializable {
    }

    /**
     * A command to add an item to the cart.
     * <p>
     * It can reply with `Confirmation`, which is sent back to the caller when
     * all the events emitted by this command are successfully persisted.
     */
    public static class CreateThingCommand extends MonitoredThingverseCommand {
        public final String thingID;
        public final Map<String, Object> attributes;
        public final ActorRef<Confirmation> replyTo;

        public CreateThingCommand(String thingID, Map<String, Object> attributes, ActorRef<Confirmation> replyTo) {
            this.thingID = thingID;
            this.attributes = attributes;
            this.replyTo = replyTo;
        }

        @Override
        public String toString() {
            return "CreateThingCommand{" +
                    "thingID='" + thingID + '\'' +
                    ", attributes=" + attributes +
                    ", replyTo=" + replyTo +
                    '}';
        }
    }

    public static class UpdateThingCommand extends MonitoredThingverseCommand {
        public final String thingID;
        public final Map<String, Object> attributes;
        public final ActorRef<Confirmation> replyTo;

        public UpdateThingCommand(String thingID, Map<String, Object> attributes, ActorRef<Confirmation> replyTo) {
            this.thingID = thingID;
            this.attributes = attributes;
            this.replyTo = replyTo;
        }
    }

    public static class GoodBye implements ThingverseCommand {
    }

    /**
     * A command to get the current state of the shopping cart.
     * <p>
     * The reply type is the {@link Summary}
     */
    public static class Get extends MonitoredThingverseCommand {
        public final ActorRef<Summary> replyTo;

        @JsonCreator
        public Get(ActorRef<Summary> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static class ClearThingCommand extends MonitoredThingverseCommand {
        public final ActorRef<ThingClearedSummary> replyTo;

        @JsonCreator
        public ClearThingCommand(ActorRef<ThingClearedSummary> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static class GetPassivationSummary extends MonitoredThingverseCommand {
        public final ActorRef<PassivationSummary> replyTo;

        @JsonCreator
        public GetPassivationSummary(ActorRef<PassivationSummary> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static class Ping extends MonitoredThingverseCommand {
        public final ActorRef<Pong> replyTo;

        @JsonCreator
        public Ping(ActorRef<Pong> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static final class ThingCreated implements ThingverseEvent {
        public final String thingID;
        public final Map<String, Object> attributes;

        public ThingCreated(String thingID, Map<String, Object> attributes) {
            this.thingID = thingID;
            this.attributes = attributes;
        }

        @Override
        public String toString() {
            return "ThingCreated(" + thingID + ")";
        }
    }

    public static final class ThingUpdated implements ThingverseEvent {
        public final String thingID;
        public final Map<String, Object> attributes;

        public ThingUpdated(String thingID, Map<String, Object> attributes) {
            this.thingID = thingID;
            this.attributes = attributes;
        }

        @Override
        public String toString() {
            return "ThingUpdated(" + thingID + ")";
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class ThingCleared implements ThingverseEvent {

        @JsonCreator
        public ThingCleared() {
        }

        @Override
        public String toString() {
            return "ThingCleared()";
        }
    }

    public static final class Summary implements CborSerializable {
        public final Map<String, Object> attributes;
        public final String thingID;

        public Summary(String thingID, Map<String, Object> attributes) {
            // Summary is included in messages and should therefore be immutable
            this.thingID = thingID;
            this.attributes = Collections.unmodifiableMap(new HashMap<>(attributes));
        }
    }

    public static final class PassivationSummary implements CborSerializable {
        public String message;

        @JsonCreator
        public PassivationSummary(String message) {
            this.message = message;
        }
    }

    public static final class Pong implements CborSerializable {
        public boolean found;

        @JsonCreator
        public Pong(boolean found) {
            this.found = found;
        }
    }

    public static final class ThingClearedSummary implements CborSerializable {
        public String message;

        @JsonCreator
        public ThingClearedSummary(String message) {
            this.message = message;
        }
    }

    public static class Accepted implements Confirmation {
        public final Summary summary;

        @JsonCreator
        public Accepted(Summary summary) {
            this.summary = summary;
        }
    }

    public static class Rejected implements Confirmation {
        public final String reason;

        @JsonCreator
        public Rejected(String reason) {
            this.reason = reason;
        }
    }

    @SuppressWarnings("unused")
    private class RemoteThingSignalHandlers {

        public void onSnapshotFailed(State state, Signal signal) {
            LOGGER.error("TODO: add some on-snapshot-failed side-effect here");
            //throw new RuntimeException("TODO: add some on-snapshot-failed side-effect here");
        }

        public void onRecoveryFailed(State state, Signal signal) {
            LOGGER.error("TODO: add some on-recovery-failed side-effect here");
            //throw new RuntimeException("TODO: add some on-recovery-failed side-effect here");
        }


        public void onDeleteSnapshotsFailed(State state, Signal signal) {
            LOGGER.error("TODO: add some on-snapshot-failed side-effect here");
            //throw new RuntimeException("TODO: add some on-snapshot-failed side-effect here");
        }

        public void onDeleteEventsFailed(State state, Signal signal) {
            LOGGER.error("TODO: add some on-snapshot-failed side-effect here");
            //throw new RuntimeException("TODO: add some on-snapshot-failed side-effect here");
        }
    }

    public final class State implements CborSerializable {
        private Map<String, Object> attributes = new HashMap<>();
        // Indicates if this thing was formally 'created' at all. We must follow the create -> get/update/stop
        // lifecycle.
        private boolean created = false;

        public State updateThing(Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }

        public State createThing(Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            this.created = true;
            return this;
        }

        public State clearThing() {
            this.attributes.clear();
            return this;
        }

        public boolean isCreated() {
            return created;
        }

        public Summary toSummary() {
            return new Summary(thingID, attributes);
        }
    }

    private class RemoteThingCommandHandlers {

        public ReplyEffect<ThingverseEvent, State> onCreateThing(State state, CreateThingCommand cmd) {
            //LOGGER.info("TRACE>> RemoteThing actor received CreateThingCommand: {}", cmd.toString());
            return Effect().persist(new ThingCreated(thingID, cmd.attributes))
                    .thenReply(cmd.replyTo, updatedThing -> new Accepted(updatedThing.toSummary()));
        }

        public ReplyEffect<ThingverseEvent, State> onUpdateThing(State state, UpdateThingCommand cmd) {
            return Effect().persist(new ThingUpdated(thingID, cmd.attributes))
                    .thenReply(cmd.replyTo, updatedThing -> new Accepted(updatedThing.toSummary()));
        }

        public ReplyEffect<ThingverseEvent, State> onGoodBye(State state, GoodBye cmd) {
            return Effect().none()
                    .thenStop()
                    .thenNoReply();
        }

        public ReplyEffect<ThingverseEvent, State> onShutdownThing(State state, GetPassivationSummary cmd) {
            return Effect().none()
                    .thenRun(() -> shard.tell(new ClusterSharding.Passivate<>(getActorContext().getSelf())))
                    .thenReply(cmd.replyTo, updatedThing -> new PassivationSummary("Thing was passivated"));
        }

        public ReplyEffect<ThingverseEvent, State> onClearThing(State state, ClearThingCommand cmd) {
            return Effect().persist(new ThingCleared())
                    .thenReply(cmd.replyTo, updatedThing -> new ThingClearedSummary("Thing was cleared"));
        }

        public ReplyEffect<ThingverseEvent, State> onIdle(State state, Idle cmd) {
            return Effect().none()
                    .thenRun(() -> shard.tell(new ClusterSharding.Passivate<>(getActorContext().getSelf())))
                    .thenNoReply();
        }

        public ReplyEffect<ThingverseEvent, State> onPing(State state, Ping cmd) {
            return Effect().none()
                    .thenReply(cmd.replyTo, s -> new Pong(s.isCreated()));
        }
    }
}
