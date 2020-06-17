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
