package com.thingverse.common.env.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import thingverse.common.config.ThingverseBaseProperties;

import java.util.*;

import static com.thingverse.common.env.health.HealthChecker.THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME;
import static com.thingverse.common.env.health.HealthChecker.THINGVERSE_HEALTH_CHECK_SYSTEM_PROPERTY_SOURCE_NAME;
import static com.thingverse.common.utils.ConsoleColors.*;

public class EnvironmentHealthListenerImpl implements EnvironmentHealthListener, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentHealthListenerImpl.class);
    public final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 2;
    private final ApplicationContext context;
    private final ThingverseBaseProperties thingverseBaseProperties;

    public EnvironmentHealthListenerImpl(ApplicationContext context, ThingverseBaseProperties thingverseBaseProperties) {
        this.context = context;
        this.thingverseBaseProperties = thingverseBaseProperties;
    }

    private void resetHealthStatusIfNeeded(ApplicationReadyEvent appReadyEvent) {

        ConfigurableEnvironment env = appReadyEvent.getApplicationContext().getEnvironment();
        // what's the current state? It can be:
        // 1. THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME not found in environment
        // 2. THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME found and its value is true (indicating healthy)
        // 3. THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME found and it's value is false (indicating unhealthy)
        // In the first case, we punch a UP value.
        // In the second case, we do nothing.
        // In the third case, we replace the DOWN value with UP

        // Now we can safely reset
        Map<String, Object> thingverseAppHealthProperties = new LinkedHashMap<>();
        thingverseAppHealthProperties.put(THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME, true);
        PropertySource<?> replacementPropertySource =
                new MapPropertySource(THINGVERSE_HEALTH_CHECK_SYSTEM_PROPERTY_SOURCE_NAME, thingverseAppHealthProperties);

        if (env.containsProperty(THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME)) {
            // Well we are guaranteed to find a value, and the default value is actually not required.
            boolean rightNowHealthy = env
                    .getProperty(THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME, Boolean.class, false);
            if (rightNowHealthy) {
                return; // no need to reset, we are already healthy
            }
            // Now we can safely reset
            LOGGER.info("Health checks have been disabled. Forcibly switching app health status from DOWN to UP.");
            env.getPropertySources().replace(THINGVERSE_HEALTH_CHECK_SYSTEM_PROPERTY_SOURCE_NAME, replacementPropertySource);
        }
    }


    @Override
    @EventListener
    public void handleApplicationReadyEvent(ApplicationReadyEvent appReadyEvent) {
        if (!thingverseBaseProperties.isHealthCheckEnabled()) {
            // We must reset any earlier DOWN status and return
            resetHealthStatusIfNeeded(appReadyEvent);
            return;
        }
        boolean currentlyHealthy = appReadyEvent.getApplicationContext().getEnvironment()
                .getProperty(THINGVERSE_APP_HEALTH_STATUS_PROPERTY_NAME, Boolean.class, true);

        // TODO WIP
        List<String> healthReportList = new ArrayList<>();
        MapPropertySource mps = (MapPropertySource) appReadyEvent.getApplicationContext().getEnvironment()
                .getPropertySources().get(THINGVERSE_HEALTH_CHECK_SYSTEM_PROPERTY_SOURCE_NAME);
        if (null != mps) {
            for (String healthProp : mps.getPropertyNames()) {
                StringTokenizer st = new StringTokenizer(healthProp, "#");
                if (st.countTokens() == 2) {
                    st.nextToken();
                    String resourceType = st.nextToken();
                    HealthStatus healthStatus = (HealthStatus) mps.getProperty(healthProp);
                    String healthReport = resourceType + ":" + healthStatus.name();
                    healthReportList.add(healthReport);
                }
            }
        }
        String reportCard = "[" + String.join(", ", healthReportList) + "]";
        LOGGER.info(thanos("Resource health Status: {}"), reportCard);

        if (currentlyHealthy) {
            LOGGER.info(hulk("All resources are UP, application started."));
        } else {
            if (thingverseBaseProperties.isTerminateOnAppUnhealthy()) {
                LOGGER.info(ironman("{}, terminating application..."), reportCard);
                SpringApplication.exit(context, () -> 0);
            } else {
                LOGGER.info(ironman("Some resources are DOWN, but the termination switch has been disabled. " +
                        "The app is going to enter an indeterminate state now."));
            }
        }
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
