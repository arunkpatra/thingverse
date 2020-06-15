package thingverse.monitoring.service;

import io.micrometer.core.instrument.Meter;

import java.util.Map;

public interface MeterRegistrar {
    Map<String, Meter> getMeterMap();
}
