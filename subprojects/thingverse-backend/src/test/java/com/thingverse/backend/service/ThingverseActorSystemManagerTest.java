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

package com.thingverse.backend.service;

import com.thingverse.backend.AbstractTest;
import com.thingverse.backend.models.ActorSystemInfo;
import com.thingverse.backend.models.ActorSystemInfoFormatted;
import com.thingverse.backend.services.ThingverseActorSystemManager;
import com.thingverse.backend.services.impl.ThingverseActorSystemManagerImpl;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ThingverseActorSystemManagerTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseActorSystemManagerTest.class);

    @Autowired
    private ThingverseActorSystemManager thingverseActorSystemManager;

    @Test
    public void doTest() throws Exception {
        ActorSystemInfo info = thingverseActorSystemManager.getActorSystemInfo();
        Assert.assertTrue(FAILURE_CHAR + "Expected actor system", info.getActorSystem().isPresent());

        ThingverseActorSystemManagerImpl impl = (ThingverseActorSystemManagerImpl) thingverseActorSystemManager;
        ActorSystemInfoFormatted fmt = impl.getActorSystemInfoFormatted();
        Assert.assertEquals(FAILURE_CHAR + "Unexpected match", "thingverse-backend", fmt.getActorSystemName());
        Assert.assertNotNull(fmt.getAddress());
        Assert.assertNotNull(fmt.getRoles());
        Assert.assertNotNull(fmt.getStartTime());
        Assert.assertNotNull(fmt.getUpTime());
        Assert.assertNotNull(fmt.getStatus());
        Assert.assertNotNull(fmt.toString());

        Assert.assertNotNull(impl.getActorSystemSettings());
        Assert.assertNotNull(impl.getActorSystemTree());
    }
}
