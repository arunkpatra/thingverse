package com.thingverse.kubernetes.env.health;

import com.thingverse.common.env.health.HealthChecker;
import com.thingverse.common.env.health.HealthStatus;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.util.Map;

/**
 * This checker just checks if the application is running inside a Kubernetes managed container or not. This is useful
 * when you want to fail fast.
 */
public class KubernetesHealthChecker implements HealthChecker {

    private final static int K8S_POD_READ_CHECK_TIMEOUT_SECONDS = 20;
    private final static String K8S_THINGVERSE_DEFAULT_NAMESPACE = "default";
    public final static String K8S_THINGVERSE_NAMESPACE_KEY = "thingverse.kubernetes.namespace";
    public final static String K8S_THINGVERSE_CHECK_POD_READ_ACCESS_KEY = "thingverse.kubernetes.check.pod.read.access";

    @Override
    public CheckResult checkHealth(ConfigurableEnvironment env, Map<String, Object> properties, DeferredLog logger) {
        CheckResult checkResult = new CheckResult(this.getClass().getName(), HealthStatus.UP, this.getResourceType());

        if (!env.containsProperty("KUBERNETES_SERVICE_HOST")) {
            checkResult = new CheckResult(this.getClass().getName(), HealthStatus.DOWN, this.getResourceType());
        }
        // This check is conditional
        String namespaceToLookup = K8S_THINGVERSE_DEFAULT_NAMESPACE;
        if (podsReadAccessNeeded(properties)) {
            try {
                namespaceToLookup =
                        (String) properties.getOrDefault(K8S_THINGVERSE_NAMESPACE_KEY, K8S_THINGVERSE_DEFAULT_NAMESPACE);
                ApiClient client = Config.defaultClient();
                Configuration.setDefaultApiClient(client);
                CoreV1Api api = new CoreV1Api();
                V1PodList list = api.listNamespacedPod(namespaceToLookup,
                        "true",
                        null,
                        null,
                        null,
                        null,
                        null,
                        K8S_POD_READ_CHECK_TIMEOUT_SECONDS,
                        null);
                for (V1Pod item : list.getItems()) {
                    logger.info("Found Pod: " + item.getMetadata().getName() + " in Namespace: " +
                            item.getMetadata().getNamespace());
                }
            } catch (ApiException | IOException e) {
                logger.error("Unable to read Pods in namespace " + namespaceToLookup + ". Either you don't have a DNS " +
                        "service available on your Kubernetes cluster or you do not have the ClusterRolBinding defined " +
                        "for the `thingverse-svc-account` service account. Can't complete Akka Cluster Bootstrap " +
                        "process, hence exiting. Kubernetes has reported this error: " + e.getMessage());
                checkResult = new CheckResult(this.getClass().getName(), HealthStatus.DOWN, this.getResourceType());
            }
        }
        return checkResult;
    }

    @Override
    public String getResourceType() {
        return "KUBERNETES";
    }

    private boolean podsReadAccessNeeded(Map<String, Object> properties) {
        return (Boolean) properties.getOrDefault(K8S_THINGVERSE_CHECK_POD_READ_ACCESS_KEY, false);
    }
}
