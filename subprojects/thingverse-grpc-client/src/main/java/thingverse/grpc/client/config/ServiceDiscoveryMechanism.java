package thingverse.grpc.client.config;

public enum ServiceDiscoveryMechanism {

    /**
     * Discover the gRPC service using a static lookup.
     */
    STATIC,

    /**
     * Discover the gRPC service by looking up Consul.
     */
    CONSUL,

    /**
     * Discover the gRPC service by using Akka DNS
     */
    DNS,

    /**
     * Discover using kubernetes service - kubernetes-service discovery
     */
    KUBERNETES_SERVICE
}
