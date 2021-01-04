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

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.propagation.B3TextMapCodec;
import io.jaegertracing.internal.reporters.LoggingReporter;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;
import thingverse.tracing.aspects.TracedAspect;

@Configuration
@EnableConfigurationProperties(ThingverseTracingProperties.class)
public class ThingverseTracingConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseTracingConfiguration.class);

    private final ThingverseTracingProperties properties;

    public ThingverseTracingConfiguration(ThingverseTracingProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "thingverse.tracing", name = {"enabled"})
    public Tracer jaegerTracer() {
        B3TextMapCodec b3Codec = new B3TextMapCodec.Builder().build();
        io.jaegertracing.Configuration.CodecConfiguration codecConfig =
                new io.jaegertracing.Configuration.CodecConfiguration();
        for (io.jaegertracing.Configuration.Propagation p : properties.getJaegerPropagationFormats()) {
            codecConfig = codecConfig.withPropagation(p);
        }

        io.jaegertracing.Configuration.SamplerConfiguration samplerConfig =
                io.jaegertracing.Configuration.SamplerConfiguration.fromEnv()
                        .withType(properties.getJaegerSamplerType().name().toLowerCase())
                        .withParam(properties.getJaegerSamplerParam());

        io.jaegertracing.Configuration.SenderConfiguration senderConfiguration =
                io.jaegertracing.Configuration.SenderConfiguration.fromEnv()
                        .withAgentHost(properties.getJaegerAgentHost())
                        .withAgentPort(properties.getJaegerAgentPort());

        if (!StringUtils.hasText(properties.getJaegerEndpoint())) {
            senderConfiguration = senderConfiguration.withEndpoint(properties.getJaegerEndpoint());
        }

        io.jaegertracing.Configuration.ReporterConfiguration reporterConfig =
                io.jaegertracing.Configuration.ReporterConfiguration.fromEnv()
                        .withMaxQueueSize(properties.getJaegerReporterMaxQueueSize())
                        .withFlushInterval(properties.getJaegerReporterFlushInterval())
                        .withLogSpans(properties.isJaegerReporterLogSpans())
                        .withSender(senderConfiguration);

        io.jaegertracing.Configuration config =
                new io.jaegertracing.Configuration(properties.getServiceName())
                        .withCodec(codecConfig)
                        .withSampler(samplerConfig)
                        .withReporter(reporterConfig);
        JaegerTracer jaegerTracer;
        if (properties.isTransmitSpan()) {
            jaegerTracer = config.getTracerBuilder()
                    .registerInjector(Format.Builtin.HTTP_HEADERS, b3Codec)
                    .registerExtractor(Format.Builtin.HTTP_HEADERS, b3Codec).build();
        } else {
            // useful in tests
            LoggingReporter loggingReporter = new LoggingReporter(LOGGER);
            jaegerTracer = config.getTracerBuilder()
                    .withReporter(loggingReporter)
                    .registerInjector(Format.Builtin.HTTP_HEADERS, b3Codec)
                    .registerExtractor(Format.Builtin.HTTP_HEADERS, b3Codec).build();
        }

        return jaegerTracer;
    }


    @Bean
    @Primary
    @ConditionalOnBean({Tracer.class})
    public ThingverseTracer thingverseTracer(Tracer tracer) {
        return new ThingverseTracer(properties.isEnabled(), tracer);
    }

    @Bean
    @ConditionalOnMissingBean({ThingverseTracer.class})
    public ThingverseTracer thingverseNoopTracer() {
        return new ThingverseTracer(false, null);
    }

    @Bean("tracedAspect")
    @ConditionalOnProperty(prefix = "thingverse.tracing", name = {"enabled"})
    public TracedAspect tracedAspect(ThingverseTracer tracer) {
        LOGGER.info("Instantiating TracedAspect bean.");
        return new TracedAspect(tracer);
    }
}
