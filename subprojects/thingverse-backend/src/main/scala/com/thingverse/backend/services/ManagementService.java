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

package com.thingverse.backend.services;

import akka.actor.typed.ActorSystem;

public interface ManagementService {

    ManagementServerInfo startManagementServer(ActorSystem<Void> actorSystem);

    class ManagementServerInfo {
        public final String host;
        public final int port;
        public final boolean error;

        public ManagementServerInfo(String host, int port, boolean error) {
            this.host = host;
            this.port = port;
            this.error = error;
        }

        @Override
        public String toString() {
            return "ManagementServerInfo{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    ", error=" + error +
                    '}';
        }
    }
}
