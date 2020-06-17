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

package com.thingverse.api.metrics;

import com.thingverse.api.services.ThingService;
import com.thingverse.common.env.health.ResourcesHealthyCondition;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import thingverse.monitoring.annotation.MetricsCollector;
import thingverse.monitoring.api.MetricsSupplier;

@Configuration
@Conditional({ResourcesHealthyCondition.class})
public class ThingverseMetricsConfiguration {

    @MetricsCollector
    public class ThingverseMetricsSupplier implements MetricsSupplier {

        private final ThingService thingService;

        public ThingverseMetricsSupplier(ThingService thingService) {
            this.thingService = thingService;
        }

        @Override
        public Meter[] registerMeters(MeterRegistry meterRegistry) {
            return new Meter[]{
                    Gauge.builder("thingverse.active_thing_count", () -> {
                        try {
                            return thingService.getActorMetricsResponse().getActiveThingCount();
                        } catch (Throwable e) {
                            return 0L;
                        }
                    }).description("Number of active Things").register(meterRegistry),
                    Gauge.builder("thingverse.average_message_age", () -> {
                        try {
                            return thingService.getActorMetricsResponse().getAverageMessageAge();
                        } catch (Throwable e) {
                            return 0L;
                        }
                    }).description("Average message age in mailbox").register(meterRegistry),
                    Gauge.builder("thingverse.total_messages_received", () -> {
                        try {
                            return thingService.getActorMetricsResponse().getTotalMessagesReceived();
                        } catch (Throwable e) {
                            return 0L;
                        }
                    }).description("Total messages received").register(meterRegistry)
            };
        }
    }
}
