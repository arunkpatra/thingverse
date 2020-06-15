package com.thingverse.backend.services;

import akka.actor.typed.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface GrpcServerBindingService {

    GrpcServerBindingStatuses bindAndServe(ActorSystem<Void> sys, Function<HttpRequest,
            CompletionStage<HttpResponse>>[] thingverseServiceHandler);

    class GrpcServerBindingStatus {
        public final String host;
        public final int port;
        public final boolean error;
        public final String portName;

        public GrpcServerBindingStatus(String host, int port, boolean error, String portName) {
            this.host = host;
            this.port = port;
            this.error = error;
            this.portName = portName;
        }

        @Override
        public String toString() {
            return "GrpcServerBindingStatus{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    ", error=" + error +
                    ", portName='" + portName + '\'' +
                    '}';
        }
    }

    class GrpcServerBindingStatuses {
        private final List<GrpcServerBindingStatus> serverBindingStatusList = new ArrayList();

        public List<GrpcServerBindingStatus> getServerBindingStatusList() {
            return serverBindingStatusList;
        }
    }
}
