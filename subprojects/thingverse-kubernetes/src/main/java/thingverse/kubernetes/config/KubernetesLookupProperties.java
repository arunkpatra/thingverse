package thingverse.kubernetes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse.kubernetes")
public class KubernetesLookupProperties {

    private static final String DEFAULT_K8S_POD_LOOKUP_NAMESPACE = "thingverse";
    private static final String DEFAULT_K8S_SERVICE_LOOKUP_NAMESPACE = "thingverse";

    /**
     * Switch to enable/disable auto-configuration.
     */
    private boolean enabled = true;

    /**
     * The K8s pod lookup namespace
     */
    private String podLookupNamespace = DEFAULT_K8S_POD_LOOKUP_NAMESPACE;
    /**
     * The K8s service lookup namespace
     */
    private String serviceLookupNamespace = DEFAULT_K8S_SERVICE_LOOKUP_NAMESPACE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPodLookupNamespace() {
        return podLookupNamespace;
    }

    public void setPodLookupNamespace(String podLookupNamespace) {
        this.podLookupNamespace = podLookupNamespace;
    }

    public String getServiceLookupNamespace() {
        return serviceLookupNamespace;
    }

    public void setServiceLookupNamespace(String serviceLookupNamespace) {
        this.serviceLookupNamespace = serviceLookupNamespace;
    }
}
