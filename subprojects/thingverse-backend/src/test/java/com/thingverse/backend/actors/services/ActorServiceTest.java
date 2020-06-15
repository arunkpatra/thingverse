package com.thingverse.backend.actors.services;

import akka.actor.typed.ActorSystem;
import akka.stream.javadsl.Sink;
import com.thingverse.backend.AbstractTest;
import com.thingverse.backend.actors.RemoteThing;
import com.thingverse.backend.models.CreateThing;
import com.thingverse.backend.models.ThingverseActorMetrics;
import com.thingverse.backend.models.UpdateThing;
import com.thingverse.backend.services.ActorService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.mockito.ArgumentMatchers.any;

public class ActorServiceTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActorServiceTest.class);
    private static Long timeOut = 20000L;
    @Autowired
    private ActorService actorService;

    @Autowired
    @Qualifier("thingverseBackendActorSystem")
    private ActorSystem<Void> actorSystem;

    private ActorService mockedActorService = Mockito.mock(ActorService.class);

    @Test
    public void createThingTest() throws Exception {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Hello Thing");
        Optional<String> thingID = createThing(attributes);
        Assert.assertTrue(FAILURE_CHAR + String.format("Thing was not created in %d millis.", timeOut), thingID.isPresent());
        LOGGER.info(SUCCESS_CHAR + "Thing created with ID {}", thingID.get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void createThingRejectedRequestTest() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Hello Thing");
        CompletionStage<RemoteThing.Confirmation> mockRejectedResponse = CompletableFuture.supplyAsync(() -> new RemoteThing.Rejected("Some god damn reason"));
        Mockito.when(mockedActorService.createThing((any(CreateThing.class)))).thenReturn(mockRejectedResponse);

        Optional<String> errorMessage =
                mockedActorService.createThing(new CreateThing(UUID.randomUUID().toString(), attributes))
                        .handleAsync((confirmation, t) -> (confirmation instanceof RemoteThing.Accepted) ?
                                Optional.of(((RemoteThing.Accepted) confirmation).summary.thingID) :
                                Optional.of(((RemoteThing.Rejected) confirmation).reason))
                        .toCompletableFuture().get();
        Assert.assertEquals(FAILURE_CHAR + "Error Message did not match", errorMessage.get(), "Some god damn reason");
    }

    @Test
    public void getThingTest() throws Exception {

        // Create
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Hello Thing");
        Optional<String> thingID = createThing(attributes);
        Assert.assertTrue(FAILURE_CHAR + "Did not get thingID after creating thing", thingID.isPresent());
        LOGGER.info(SUCCESS_CHAR + "Thing with ID {} was created.", thingID.get());

        Optional<RemoteThing.Summary> thingSummary = getThing(thingID.get());
        Assert.assertTrue(FAILURE_CHAR + "Did not get response summary", thingSummary.isPresent());
        Assert.assertFalse(FAILURE_CHAR + String.format("Thing with ID %s was not found", thingID.get()),
                "not-found".contentEquals(thingSummary.get().thingID));
        LOGGER.info(SUCCESS_CHAR + "Thing with ID {} was retrieved.", thingID.get());
    }

    @Test
    public void updateThingTest() throws Exception {
        // Create thing
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Hello Thing");
        Optional<String> thingID = createThing(attributes);
        Assert.assertTrue(FAILURE_CHAR + "Did not get thingID after creating thing", thingID.isPresent());
        // Update
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put("temp", 42);
        newAttributes.put("vibration", 1200);

        Optional<RemoteThing.Confirmation> updateThingResponse = updateThing(thingID.get(), newAttributes);
        Assert.assertTrue(FAILURE_CHAR + "Did not get updateThingResponse", updateThingResponse.isPresent());
        Assert.assertTrue(FAILURE_CHAR + "Response was not of Accepted type.",
                updateThingResponse.get() instanceof RemoteThing.Accepted);
        Assert.assertTrue(FAILURE_CHAR + "Attribute `temp` was not found",
                ((RemoteThing.Accepted) updateThingResponse.get()).summary.attributes.containsKey("temp"));
        Assert.assertEquals(FAILURE_CHAR + "Attribute `temp` value was wrong", 42,
                ((RemoteThing.Accepted) updateThingResponse.get()).summary.attributes.get("temp"));
    }

    @Test
    public void updateThingUnknownThingTest() throws Exception {
        // Update
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put("temp", 42);
        newAttributes.put("vibration", 1200);

        Optional<RemoteThing.Confirmation> updateThingResponse = updateThing("some-junk-thing-id-1", newAttributes);
        Assert.assertTrue(FAILURE_CHAR + "Did not get updateThingResponse", updateThingResponse.isPresent());
        Assert.assertTrue(FAILURE_CHAR + "Response was not of Rejected type.",
                updateThingResponse.get() instanceof RemoteThing.Rejected);
        Assert.assertEquals(FAILURE_CHAR + "Rejection reason is not correct",
                ((RemoteThing.Rejected) updateThingResponse.get()).reason, "Thing not found");
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void updateThingWithInternalExceptionTest() throws Exception {
        CompletableFuture<RemoteThing.Confirmation> mockResponseException = new CompletableFuture<>();
        mockResponseException.completeExceptionally(new RuntimeException("Junk Exception"));
        Mockito.when(mockedActorService.updateThing((any(UpdateThing.class)))).thenReturn(mockResponseException);
        // Update
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put("temp", 42);
        newAttributes.put("vibration", 1200);

        Optional<RemoteThing.Confirmation> updateThingResponse =
                mockedActorService.updateThing(new UpdateThing("junk-thing-id-2", newAttributes))
                        .handleAsync((confirmation, t) -> {
                            if (null == t) {
                                LOGGER.info("t was null");
                                return Optional.of(confirmation);
                            } else {
                                LOGGER.info("t was not null");
                                return Optional.of((RemoteThing.Confirmation) new RemoteThing.Rejected(t.getMessage()));
                            }
                        })
                        .toCompletableFuture().get();
        Assert.assertTrue(FAILURE_CHAR + "Expected rejection.", updateThingResponse.get() instanceof RemoteThing.Rejected);
        Assert.assertEquals(FAILURE_CHAR + "Error message did not match.",
                ((RemoteThing.Rejected) updateThingResponse.get()).reason, "Junk Exception");
    }

    @Test
    public void clearThingTest() throws Exception {
        // Create thing
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Hello Thing");
        Optional<String> thingID = createThing(attributes);
        Assert.assertTrue(FAILURE_CHAR + "Did not get thingID after creating thing", thingID.isPresent());
        // Clear
        Optional<RemoteThing.ThingClearedSummary> thingClearedSummary = clearThing(thingID.get());
        Assert.assertTrue(FAILURE_CHAR + "Did not get thingClearedSummary", thingClearedSummary.isPresent());
        RemoteThing.ThingClearedSummary summary = thingClearedSummary.get();
        Assert.assertEquals(FAILURE_CHAR + "Unexpected response", "Thing was cleared", summary.message);
        // get
        Optional<RemoteThing.Summary> thingSummary = getThing(thingID.get());
        Assert.assertTrue(FAILURE_CHAR + "Did not get response summary", thingSummary.isPresent());
        Assert.assertFalse(FAILURE_CHAR + String.format("Thing with ID %s was not found", thingID.get()),
                "not-found".contentEquals(thingSummary.get().thingID));
        LOGGER.info(SUCCESS_CHAR + "Thing with ID {} was retrieved.", thingID.get());
        Assert.assertEquals(FAILURE_CHAR + "Thing was not cleared correctly.", 0, thingSummary.get().attributes.size());
    }

    @Test
    public void actorMetricsTest() throws Exception {
        // Create thing
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Hello Thing");
        Optional<String> thingID = createThing(attributes);
        Assert.assertTrue(FAILURE_CHAR + "Did not get thingID after creating thing", thingID.isPresent());

        // Now get metrics
        ThingverseActorMetrics metrics = getActorMetrics();
        Assert.assertNotEquals(FAILURE_CHAR + "Wasn't expecting zero active things", 0L,
                (long) metrics.getTotalActiveThings());
        Assert.assertNotEquals(FAILURE_CHAR + "Wasn't expecting zero messages received", 0L,
                (long) metrics.getTotalMessagesReceived());
        Assert.assertNotNull(FAILURE_CHAR + "Wasn't expecting null", metrics.getTotalActiveThings());
        LOGGER.info("Obtained metrics : {}", metrics);
    }

    @Test
    public void pingTest() throws Exception {
        // Find unknown thing
        Assert.assertFalse(FAILURE_CHAR +
                        String.format("Thing with ID %s should not have been found", "unknown-thing-id"),
                pingThing("unknown-thing-id"));
        LOGGER.info(SUCCESS_CHAR + "Unknown thing was pinged and result was not found");
        // Create thing
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Hello Thing");
        Optional<String> thingID = createThing(attributes);
        Assert.assertTrue(FAILURE_CHAR + "Did not get thingID after creating thing", thingID.isPresent());
        // Find known thing
        Assert.assertTrue(FAILURE_CHAR +
                        String.format("Thing with ID %s should have been found", "unknown-thing-id"),
                pingThing(thingID.get()));

        LOGGER.info(SUCCESS_CHAR + "Known thing was pinged and result was found");
    }

    @Test
    public void getAllThingIDs() throws Exception {
        // Create
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "Hello Thing");
        Optional<String> thingID1 = createThing(attributes);
        Assert.assertTrue(FAILURE_CHAR + "Did not get thingID1 after creating thing", thingID1.isPresent());
        Optional<String> thingID2 = createThing(attributes);
        Assert.assertTrue(FAILURE_CHAR + "Did not get thingID2 after creating thing", thingID2.isPresent());

        // Check now
        List<String> thingIDList = actorService.streamAllThingIDs(100L).runWith(Sink.seq(), actorSystem)
                .handleAsync((strings, throwable) -> strings)
                .toCompletableFuture().get();
        Assert.assertTrue("Did not get thingID1 via #getAllThingIDs()", thingIDList.contains(thingID1.get()));
        Assert.assertTrue("Did not get thingID2 via #getAllThingIDs()", thingIDList.contains(thingID2.get()));

        LOGGER.info(SUCCESS_CHAR + "Retrieved thingIDs via #getAllThingIDs() successfully.");
    }

    private Optional<String> createThing(Map<String, Object> attributes) throws Exception {
        return actorService.createThing(new CreateThing(UUID.randomUUID().toString(), attributes))
                .handleAsync((confirmation, t) -> (confirmation instanceof RemoteThing.Accepted) ?
                        Optional.of(((RemoteThing.Accepted) confirmation).summary.thingID) :
                        Optional.of(((RemoteThing.Rejected) confirmation).reason))
                .toCompletableFuture().get();
    }

    private Optional<RemoteThing.Summary> getThing(String thingID) throws Exception {
        return actorService.getThing(thingID)
                .handleAsync((summary, t) -> Optional.of(summary))
                .toCompletableFuture().get();
    }

    private Optional<RemoteThing.ThingClearedSummary> clearThing(String thingID) throws Exception {
        return actorService.clearThing(thingID)
                .handleAsync((summary, t) -> Optional.of(summary))
                .toCompletableFuture().get();
    }

    private Optional<RemoteThing.Confirmation> updateThing(String thingID, Map<String, Object> attributesMap) throws Exception {
        return actorService.updateThing(new UpdateThing(thingID, attributesMap))
                .handleAsync((confirmation, t) -> Optional.of(confirmation))
                .toCompletableFuture().get();
    }

    private boolean pingThing(String thingID) throws Exception {
        return actorService.ping(thingID)
                .handleAsync((pong, t) -> pong.found)
                .toCompletableFuture().get();
    }

    private ThingverseActorMetrics getActorMetrics() throws Exception {
        return actorService.getActorMetrics()
                .handleAsync((m, t) -> m)
                .toCompletableFuture().get();
    }
}
