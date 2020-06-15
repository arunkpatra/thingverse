package storage.backend.cassandra.config;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class CassandraBackendImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{CassandraBackendConfiguration.class.getCanonicalName()};
    }
}
