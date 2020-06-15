package thingverse.monitoring.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class ThingverseMetricsImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{ThingverseMetricsConfiguration.class.getCanonicalName()};
    }
}
