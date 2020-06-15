package thingverse.monitoring.api;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Provides the source of some metrics that can be encapsulated and present via a Micrometer {@link Meter}.
 *
 * @author Arun Patra
 */
public interface MetricsSupplier {

    /**
     * Register a {@link Meter}.
     *
     * @param meterRegistry The meter registry
     * @return The registered meter
     */
    Meter[] registerMeters(MeterRegistry meterRegistry);
}
