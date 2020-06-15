package thingverse.grpc.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse.grpc.client")
public class ThingverseGrpcClientProperties {

    private static final String DEFAULT_CLIENT_NAME = "thingverse-service-client";
    private static final String DEFAULT_SERVICE_NAME = "thingverse-backend";

    /**
     * Switch to enable/disable auto-configuration.
     */
    private boolean enabled = true;
    /**
     * The service discovery mechanism to use.
     */
    private ServiceDiscoveryMechanism discoveryMechanism = ServiceDiscoveryMechanism.STATIC;
    /**
     * The client name to use.
     */
    private String clientName = DEFAULT_CLIENT_NAME;
    /**
     * The gRPC Service to look for.
     */
    private String serviceName = DEFAULT_SERVICE_NAME;
    /**
     * Whether to use TLS for gRPC communication
     */
    private boolean useTls = false;

    public boolean isUseTls() {
        return useTls;
    }

    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClientName() {
        return clientName;
    }

    public ServiceDiscoveryMechanism getDiscoveryMechanism() {
        return discoveryMechanism;
    }

    public void setDiscoveryMechanism(ServiceDiscoveryMechanism discoveryMechanism) {
        this.discoveryMechanism = discoveryMechanism;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
