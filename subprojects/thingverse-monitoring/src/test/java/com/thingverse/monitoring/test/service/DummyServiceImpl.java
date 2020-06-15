package com.thingverse.monitoring.test.service;

import com.thingverse.monitoring.test.model.MetricData;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thingverse.monitoring.annotation.Metered;

import java.util.Random;

public class DummyServiceImpl implements DummyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyServiceImpl.class);

    @Override
    @Metered(metricName = "thingverse.test_message_count", type = Counter.class)
    public MetricData getMetricData() {
        LOGGER.info("In DummyServiceImpl#getMetricData() method.");
        return new MetricData((new Random()).nextLong(), (new Random()).nextLong());
    }
}
