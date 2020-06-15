package com.thingverse.backend.services.impl;

import akka.actor.typed.ActorSystem;
import akka.management.javadsl.AkkaManagement;
import com.thingverse.backend.services.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ManagementServiceImpl implements ManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementServiceImpl.class);

    @Override
    public ManagementServerInfo startManagementServer(ActorSystem<Void> system) {
        try {
            return AkkaManagement.get(system.classicSystem()).start().handle((u, t) ->
                    null == t ?
                            new ManagementServerInfo(u.getHost().address(), u.getPort(), false) :
                            new ManagementServerInfo("", 0, true))
                    .whenCompleteAsync((u, t) -> LOGGER.info("Management Server start result: {}", u))
                    .toCompletableFuture().get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Management Server could not be started. Bailing out.", e);
        }
    }
}
