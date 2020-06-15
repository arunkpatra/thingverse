package thingverse.discovery.consul.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class ConsulRegistrationImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{ConsulRegistrationConfiguration.class.getCanonicalName()};
    }
}
