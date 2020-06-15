package thingverse.grpc.client.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class ThingverseGrpcClientImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{ThingverseGrpcClientConfiguration.class.getCanonicalName()};
    }
}
