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

package thingverse.tracing.config;

import io.jaegertracing.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse.tracing")
public class ThingverseTracingProperties {
    private static final Configuration.Propagation[] DEFAULT_PROPAGATION_FORMATS =
            {Configuration.Propagation.B3, Configuration.Propagation.JAEGER};
    private static final String DEFAULT_JAEGER_ENDPOINT = "http://localhost:14268/api/traces";
    //private final String DEFAULT_JAEGER_ENDPOINT = "";

    private Configuration.Propagation[] jaegerPropagationFormats = DEFAULT_PROPAGATION_FORMATS;
    /**
     * Instructs the Reporter to log finished span IDs. The reporter may need to be given a Logger for this option to take effect.
     */
    private boolean jaegerReporterLogSpans = true;
    /**
     * Defines the max size of the in-memory buffer used to keep spans before they are sent out.
     */
    private int jaegerReporterMaxQueueSize = 100;
    /**
     * Defines how frequently the report flushes span batches. Reporter can also flush the batch if the batch size reaches the maximum UDP packet size (~64Kb).
     */
    private int jaegerReporterFlushInterval = 2000;

    /**
     * Defines the type of sampler to use, e.g. probabilistic, or const (see Sampling in Jaeger docs).
     */
    private JaegerSamplerType jaegerSamplerType = JaegerSamplerType.CONST;

    /**
     * Provides configuration value to the sampler, e.g. probability=0.001. Has different meanings for different samplers (see Sampling in Jaeger docs).
     */
    private Number jaegerSamplerParam = 1;

    /**
     * Enable or disable tracing.
     */
    private boolean enabled = false;

    /**
     * Whether to transmit spans to the remote agent. Disabling this will cause the spans to just logged.
     */
    private boolean transmitSpan = true;
    /**
     * The service name
     */
    private String serviceName = "thingverse-api";

    private String jaegerEndpoint = DEFAULT_JAEGER_ENDPOINT;
    /**
     * Jaeger agent host
     */
    private String jaegerAgentHost = "localhost";

    /**
     * Jaeger agent port
     */
    private int jaegerAgentPort = 14268;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getJaegerAgentHost() {
        return jaegerAgentHost;
    }

    public void setJaegerAgentHost(String jaegerAgentHost) {
        this.jaegerAgentHost = jaegerAgentHost;
    }

    public int getJaegerAgentPort() {
        return jaegerAgentPort;
    }

    public void setJaegerAgentPort(int jaegerAgentPort) {
        this.jaegerAgentPort = jaegerAgentPort;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    public String getJaegerEndpoint() {
        return jaegerEndpoint;
    }

    public void setJaegerEndpoint(String jaegerEndpoint) {
        this.jaegerEndpoint = jaegerEndpoint;
    }

    public boolean isTransmitSpan() {
        return transmitSpan;
    }

    public void setTransmitSpan(boolean transmitSpan) {
        this.transmitSpan = transmitSpan;
    }

    public JaegerSamplerType getJaegerSamplerType() {
        return jaegerSamplerType;
    }

    public void setJaegerSamplerType(JaegerSamplerType jaegerSamplerType) {
        this.jaegerSamplerType = jaegerSamplerType;
    }

    public Number getJaegerSamplerParam() {
        return jaegerSamplerParam;
    }

    public void setJaegerSamplerParam(Number jaegerSamplerParam) {
        this.jaegerSamplerParam = jaegerSamplerParam;
    }

    public boolean isJaegerReporterLogSpans() {
        return jaegerReporterLogSpans;
    }

    public void setJaegerReporterLogSpans(boolean jaegerReporterLogSpans) {
        this.jaegerReporterLogSpans = jaegerReporterLogSpans;
    }

    public int getJaegerReporterMaxQueueSize() {
        return jaegerReporterMaxQueueSize;
    }

    public void setJaegerReporterMaxQueueSize(int jaegerReporterMaxQueueSize) {
        this.jaegerReporterMaxQueueSize = jaegerReporterMaxQueueSize;
    }

    public int getJaegerReporterFlushInterval() {
        return jaegerReporterFlushInterval;
    }

    public void setJaegerReporterFlushInterval(int jaegerReporterFlushInterval) {
        this.jaegerReporterFlushInterval = jaegerReporterFlushInterval;
    }

    public Configuration.Propagation[] getJaegerPropagationFormats() {
        return jaegerPropagationFormats;
    }

    public void setJaegerPropagationFormats(Configuration.Propagation[] jaegerPropagationFormats) {
        this.jaegerPropagationFormats = jaegerPropagationFormats;
    }

    public enum JaegerSamplerType {
        CONST, PROBABILISTIC, RATELIMITING, REMOTE
    }
}
