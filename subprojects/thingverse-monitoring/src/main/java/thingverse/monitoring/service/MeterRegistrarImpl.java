package thingverse.monitoring.service;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import thingverse.monitoring.annotation.MetricsCollector;
import thingverse.monitoring.api.MetricsSupplier;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

public class MeterRegistrarImpl implements MeterRegistrar {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeterRegistrarImpl.class);
    private final MeterRegistry meterRegistry;
    private final ApplicationContext applicationContext;
    private final Map<String, Meter> meterMap = new Hashtable<>();

    public MeterRegistrarImpl(ApplicationContext applicationContext, MeterRegistry meterRegistry) {
        this.applicationContext = applicationContext;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Discover and register all meters supplied by MetricsSupplier beans.
        applicationContext.getBeansWithAnnotation(MetricsCollector.class).values()
                .stream()
                .filter(o -> o instanceof MetricsSupplier)
                .map(s -> (MetricsSupplier) s)
                .map(s -> s.registerMeters(meterRegistry))
                .forEach(meters -> Arrays.asList(meters).forEach(m -> meterMap.put(m.getId().getName(), m)));
        // print
        printReport();
    }

    private void printReport() {
        if (meterMap.size() > 0) {
            System.out.println(getReportData());
        }
    }

    private String getReportData() {
        StringBuffer sb = new StringBuffer();
        sb
                .append("------------------------------------------------------------------------------------")
                .append("\nMeter List                        ")
                .append("\n------------------------------------------------------------------------------------");
        meterMap.forEach((name, meter) -> {
            sb.append("\nMeter name: ").append(name).append(", Class: ").append(meter.getClass().getName());
        });
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public Map<String, Meter> getMeterMap() {
        return this.meterMap;
    }
}
