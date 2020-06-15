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
