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

package com.thingverse.common.env.postprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

public abstract class AbstractOperationModeOverridesInjector implements OperationModeOverridesInjector, OnceOnlyEnvProcessor {

    protected static final DeferredLog logger = new DeferredLog();
    protected static final String THINGVERSE_ENV_KEY = "thingverse-env";
    protected static final String RUNTIME_ENVIRONMENT_KEY = "runtime-env";
    protected static final String RUNTIME_ENVIRONMENT_LOCAL = "local";
    protected static final String RUNTIME_ENVIRONMENT_K8S = "kubernetes";
    private static final String OPERATION_MODE_KEY = "operation-mode";
    private static final String OPERATION_MODE_STANDALONE = "standalone";
    private static final String OPERATION_MODE_CLUSTER = "cluster";
    private final List<String> allowedModes = Arrays.asList(OPERATION_MODE_STANDALONE, OPERATION_MODE_CLUSTER);

    @Override
    public void switchToImmediateLogger() {
        logger.switchTo(this.getClass());
    }

    @Override
    public DeferredLog getLogger() {
        return logger;
    }

    @Override
    public Map<String, Object> getClusterModeOverrides(ConfigurableEnvironment environment) {
        return new LinkedHashMap<>(); // Sub-classes may supply overrides as needed
    }

    @Override
    public Map<String, Object> getStandaloneModeOverrides(ConfigurableEnvironment environment) {
        return new LinkedHashMap<>(); // Sub-classes may supply overrides as needed
    }

    @Override
    public AtomicInteger executionCount() {
        return new AtomicInteger(0);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
//        if (this.executionCount().getAndIncrement() > 0) {
//            logger.info("This Environment post processor has already executed.");
//            return;
//        }
        logger.info(String.format("%s started post-processing at %s", this.getClass().getSimpleName(),
                (new Date()).toInstant().toString()));

        postProcessInternal(environment, application);

        logger.info(String.format("%s completed post-processing at %s", this.getClass().getSimpleName(),
                (new Date()).toInstant().toString()));
    }

    private void postProcessInternal(ConfigurableEnvironment environment, SpringApplication application) {
        logger.info("In postProcessInternal");
        PropertySource<?> system = environment.getPropertySources().get(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        if (hasOperationMode(system)) {
            String userSuppliedModeString = (String) system.getProperty(OPERATION_MODE_KEY);
            logger.info(String.format("System property operation-mode=%s detected, will apply configured overrides.",
                    userSuppliedModeString));
            if (!allowedModes.contains(userSuppliedModeString)) {
                logger.warn(String.format("Invalid operation mode '%s'. Allowed values for %s are %s.",
                        userSuppliedModeString, OPERATION_MODE_KEY, allowedModes));
                return;
            }

            Map<String, Object> thingverseOverrides = new LinkedHashMap<>();
            if (OPERATION_MODE_CLUSTER.equalsIgnoreCase(userSuppliedModeString)) {
                // do our stuff
                logger.info("This is cluster mode.");
                thingverseOverrides = getClusterModeOverrides(environment);
            } else {
                if (OPERATION_MODE_STANDALONE.equalsIgnoreCase(userSuppliedModeString)) {
                    logger.info("This is standalone mode.");
                    thingverseOverrides = getStandaloneModeOverrides(environment);
                }
            }
            if (thingverseOverrides.size() > 0) {
                logger.info(String.format("Applied these overrides which will have highest priority: %s", thingverseOverrides.toString()));
                environment.getPropertySources()
                        .addAfter(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                                new MapPropertySource("thingverseOverrides", thingverseOverrides));
            } else {
                logger.info("Did not find any overrides to apply.");
            }
        }
        getInfoTags(environment, application);
    }

    private void getInfoTags(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> tagInfoProperties = new LinkedHashMap<>();
        try (InputStream gitPropsIs = application.getClassLoader().getResourceAsStream("git.properties")) {
            if (null != gitPropsIs) {
                Properties gitProps = new Properties();
                gitProps.load(gitPropsIs);
                tagInfoProperties.put("spring.boot.admin.client.instance.metadata.tags.commit",
                        gitProps.getProperty("git.commit.id.abbrev", "-----"));
            }
        } catch (IOException e) {
            // Ignore
            getLogger().warn("Could not load git.properties file.");
        }
        try (InputStream buildPropsIs =
                     application.getClassLoader().getResourceAsStream("META-INF/build-info.properties")) {
            if (null != buildPropsIs) {
                Properties buildProps = new Properties();
                buildProps.load(buildPropsIs);
                tagInfoProperties.put("spring.boot.admin.client.instance.metadata.tags.version",
                        buildProps.getProperty("build.version", "0.0.0"));
            }
        } catch (IOException e) {
            // Ignore
            getLogger().warn("Could not load META-INF/build-info.properties file.");
        }

        if (tagInfoProperties.size() > 0) {
            environment.getPropertySources()
                    .addAfter(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                            new MapPropertySource("tagInfoProperties", tagInfoProperties));
        }
    }

    private boolean hasOperationMode(PropertySource<?> system) {
        return system.containsProperty(OPERATION_MODE_KEY);
    }

    protected boolean hasRuntimeEnv(PropertySource<?> system) {
        return system.containsProperty(RUNTIME_ENVIRONMENT_KEY);
    }
}
