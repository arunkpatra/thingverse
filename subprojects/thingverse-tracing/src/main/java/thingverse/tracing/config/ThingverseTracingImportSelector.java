package thingverse.tracing.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class ThingverseTracingImportSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{ThingverseTracingConfiguration.class.getCanonicalName()};
    }
}
