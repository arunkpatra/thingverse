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
