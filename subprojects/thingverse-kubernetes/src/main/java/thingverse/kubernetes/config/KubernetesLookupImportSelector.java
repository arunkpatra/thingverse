package thingverse.kubernetes.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class KubernetesLookupImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{KubernetesLookupConfiguration.class.getCanonicalName()};
    }
}
