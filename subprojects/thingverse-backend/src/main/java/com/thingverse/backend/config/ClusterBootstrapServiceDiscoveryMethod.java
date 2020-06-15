package com.thingverse.backend.config;

/**
 * The mode in which the Thingverse node is operated.
 */
public enum ClusterBootstrapServiceDiscoveryMethod {

    /**
     * In this case, static seed nodes are used.
     */
    STATIC,
    /**
     * Use Consul for discovering cluster members during the Cluster Bootstrap process.
     * See: https://doc.akka.io/docs/akka-management/current/discovery/consul.html
     */
    CONSUL,

    /**
     * Use Kubernetes API for discovering cluster members during the Cluster Bootstrap process.
     * See: https://doc.akka.io/docs/akka-management/current/discovery/kubernetes.html
     */
    KUBERNETES
}
