package com.thingverse.common.env.health;

import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * Provides the ability to check the health or ability of an arbitrary resource, e.g. a Cassandra instance, a Consul
 * agent or a Database server.
 *
 * @author Arun Patra
 */
public interface HealthChecker {
    String THINGVERSE_HEALTH_CHECK_SYSTEM_PROPERTY_SOURCE_NAME = "thingverseAppHealthProperties";
    String THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME = "thingverse.app.health.status";

    /**
     * Check the health of the resource represented by this checker.
     *
     * @param env        The environment object.
     * @param properties The properties sourced from the application's environment.
     * @param logger     The deferred logger. Typically health checkers run very early in the application startup cycle
     *                   even before the logging sub-system has initialized itself. The deferred logger allows to accumulate
     *                   logs and replay when needed.
     * @return {@link HealthStatus#UP} if the resource is healthy, else {@link HealthStatus#DOWN}.
     */
    CheckResult checkHealth(ConfigurableEnvironment env, Map<String, Object> properties, DeferredLog logger);

    /**
     * A friendly name that identifies the resource type, e.g. CASSANDRA, CONSUL, KUBERNETES etc.
     *
     * @return The resource type.
     */
    String getResourceType();

    /**
     * Result of a health check operation.
     */
    class CheckResult {
        public final String checkName;
        public final HealthStatus status;
        public final String checkedResourceType;

        public CheckResult(String checkName, HealthStatus status, String checkedResourceType) {
            this.checkName = checkName;
            this.status = status;
            this.checkedResourceType = checkedResourceType;
        }
    }
}
