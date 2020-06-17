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
