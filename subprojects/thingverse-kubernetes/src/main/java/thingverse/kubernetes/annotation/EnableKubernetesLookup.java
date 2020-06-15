package thingverse.kubernetes.annotation;

import org.springframework.context.annotation.Import;
import thingverse.kubernetes.config.KubernetesLookupImportSelector;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(KubernetesLookupImportSelector.class)
public @interface EnableKubernetesLookup {

}
