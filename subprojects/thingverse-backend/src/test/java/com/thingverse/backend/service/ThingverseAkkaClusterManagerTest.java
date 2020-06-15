package com.thingverse.backend.service;

import com.thingverse.backend.AbstractTest;
import com.thingverse.backend.services.ThingverseAkkaClusterManager;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ThingverseAkkaClusterManagerTest extends AbstractTest {

    @Autowired
    private ThingverseAkkaClusterManager manager;

    @Test
    public void doTest() throws Exception {
        Assert.assertNotNull(manager.getClusterMemberInfo());
        Assert.assertNotNull(manager.getClusterMemberInfo().getMembers());
        manager.getClusterMemberInfo().getMembers().forEach(m -> {
            Assert.assertNotNull(m.getAddress());
            Assert.assertNotNull(m.getDataCenter());
            Assert.assertNotNull(m.getRoles());
            Assert.assertNotNull(m.getStatus());
            Assert.assertNotNull(m.getUniqueAddress());
            Assert.assertEquals(1, m.getUpNumber());
        });
    }
}
