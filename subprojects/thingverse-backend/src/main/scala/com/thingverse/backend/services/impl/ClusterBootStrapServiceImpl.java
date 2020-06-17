package com.thingverse.backend.services.impl;

import akka.actor.typed.ActorSystem;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import com.thingverse.backend.services.ClusterBootStrapService;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ClusterBootstrap process is not required if seed nodes have already been configured via configuration. In that case,
 * this routine will exit without performing any action.
 */
public class ClusterBootStrapServiceImpl implements ClusterBootStrapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterBootStrapServiceImpl.class);

    private final ActorSystem<Void> system;

    public ClusterBootStrapServiceImpl(ActorSystem<Void> system) {
        this.system = system;
        startBootstrapProcess(system);
    }

    @Override
    public void startBootstrapProcess(ActorSystem<Void> system) {
        Config cc = system.settings().config();
        if (cc.hasPath("akka.cluster.seed-nodes")) {
            List<String> seedNodeList = cc.getStringList("akka.cluster.seed-nodes");
            if (!seedNodeList.isEmpty()) {
                LOGGER.debug("Found user configured static seed nodes: {}.", seedNodeList.toString());
                return;
            }
        }
        LOGGER.info("Looking for seed nodes using ClusterBootstrap process...");
        ClusterBootstrap.get(system.classicSystem()).start();
    }
}
