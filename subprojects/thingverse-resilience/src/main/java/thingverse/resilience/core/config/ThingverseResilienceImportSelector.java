package thingverse.resilience.core.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class ThingverseResilienceImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{ThingverseResilienceConfiguration.class.getCanonicalName()};
    }
}
