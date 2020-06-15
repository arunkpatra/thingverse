package thingverse.monitoring.aspects;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thingverse.monitoring.annotation.Metered;
import thingverse.monitoring.service.MeterRegistrar;

import java.lang.reflect.Method;

/**
 * Meter the method.
 */
@Aspect
public class MeteredAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeteredAspect.class);
    private final MeterRegistrar meterRegistrar;

    public MeteredAspect(MeterRegistrar meterRegistrar) {
        LOGGER.info("Creating MeteredAspect aspect.");
        this.meterRegistrar = meterRegistrar;
    }

    @Pointcut("@annotation(thingverse.monitoring.annotation.Metered)")
    public void meteredTarget() {
    }

    @Around(value = "meteredTarget()")
    public Object submit(ProceedingJoinPoint joinPoint) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        LOGGER.debug("[Meter] Before metering method: {}", method.toString());
        Metered metered = method.getAnnotation(Metered.class);
        String meterName = metered.metricName();

        if (meterRegistrar.getMeterMap().containsKey(meterName)) {
            Meter meter = meterRegistrar.getMeterMap().get(meterName);
            if (meter instanceof Counter) {
                ((Counter) meter).increment();
                LOGGER.debug("[Meter] Incremented counter {}", meterName);
            }
        }
        // Do real stuff
        Object proceed = joinPoint.proceed();

        LOGGER.debug("[Meter] After metering method: {}", method.toString());
        return proceed;
    }
}
