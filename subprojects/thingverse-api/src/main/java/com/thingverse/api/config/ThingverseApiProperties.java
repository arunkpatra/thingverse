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

package com.thingverse.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse.api")
public class ThingverseApiProperties {
    private static final long DEFAULT_JWT_TOKEN_VALIDITY_DURATION_SECONDS = 5 * 60 * 60;
    private static final long DEFAULT_API_CALL_TIMEOUT_MILLIS = 20000;

    private boolean tracingEnabled = false;

    /**
     * The Opneconsensus service host
     */
    private String ocCollectorServiceHost = "localhost";
    private String jaegerServiceHost = "localhost";

    /**
     * The Opneconsensus service port
     */
    private int ocCollectorServicePort = 55678;
    private int jaegerServicePort = 14268;

    /**
     * Indicate if we should check the health of the Thingverse backend cluster while calculating health.
     */
    private boolean backendHealthCheckEnabled = false;
    /**
     * For dynamically assigned HTTP server port number, the minimum port number to choose from.
     */
    private int portNumberMin = 20000;
    /**
     * For dynamically assigned HTTP server port number, the maximum port number to choose from.
     */
    private int portNumberMax = 30000;
    /**
     * JWT token validity in seconds.
     */
    private long jwtTokenValiditySeconds = DEFAULT_JWT_TOKEN_VALIDITY_DURATION_SECONDS;
    /**
     * JWT Secret key. <strong>Always inject this as an environment variables at runtime in production environments.</strong>
     */
    private String jwtSecretKey = "th1ngsc@pe$@s3cr3t";
    /**
     * The number of milli seconds to wait before the API call will timeout.
     */
    private long callTimeoutMillis = DEFAULT_API_CALL_TIMEOUT_MILLIS;
    /**
     * Switch to turn on/off API security.
     */
    private boolean secured = false;

    public boolean isBackendHealthCheckEnabled() {
        return backendHealthCheckEnabled;
    }

    public void setBackendHealthCheckEnabled(boolean backendHealthCheckEnabled) {
        this.backendHealthCheckEnabled = backendHealthCheckEnabled;
    }

    public int getPortNumberMin() {
        return portNumberMin;
    }

    public void setPortNumberMin(int portNumberMin) {
        this.portNumberMin = portNumberMin;
    }

    public int getPortNumberMax() {
        return portNumberMax;
    }

    public void setPortNumberMax(int portNumberMax) {
        this.portNumberMax = portNumberMax;
    }

    public String getJwtSecretKey() {
        return jwtSecretKey;
    }

    public void setJwtSecretKey(String jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

    public long getJwtTokenValiditySeconds() {
        return jwtTokenValiditySeconds;
    }

    public void setJwtTokenValiditySeconds(long jwtTokenValiditySeconds) {
        this.jwtTokenValiditySeconds = jwtTokenValiditySeconds;
    }

    public long getCallTimeoutMillis() {
        return callTimeoutMillis;
    }

    public void setCallTimeoutMillis(long callTimeoutMillis) {
        this.callTimeoutMillis = callTimeoutMillis;
    }

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public boolean isTracingEnabled() {
        return tracingEnabled;
    }

    public void setTracingEnabled(boolean tracingEnabled) {
        this.tracingEnabled = tracingEnabled;
    }

    public String getOcCollectorServiceHost() {
        return ocCollectorServiceHost;
    }

    public void setOcCollectorServiceHost(String ocCollectorServiceHost) {
        this.ocCollectorServiceHost = ocCollectorServiceHost;
    }

    public int getOcCollectorServicePort() {
        return ocCollectorServicePort;
    }

    public void setOcCollectorServicePort(int ocCollectorServicePort) {
        this.ocCollectorServicePort = ocCollectorServicePort;
    }

    public String getJaegerServiceHost() {
        return jaegerServiceHost;
    }

    public void setJaegerServiceHost(String jaegerServiceHost) {
        this.jaegerServiceHost = jaegerServiceHost;
    }

    public int getJaegerServicePort() {
        return jaegerServicePort;
    }

    public void setJaegerServicePort(int jaegerServicePort) {
        this.jaegerServicePort = jaegerServicePort;
    }
}
