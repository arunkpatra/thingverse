package com.thingverse.backend.config;

/**
 * The mode in which the Thingverse node is operated.
 */
public enum BackendOperationMode {

    /**
     * Initialize a standalone node that's not intended to be part of a cluster.
     */
    STANDALONE,

    /**
     * Initialize node to be able to participate in a cluster.
     */
    CLUSTER
}
