package thingverse.discovery.consul.annotation;

import org.springframework.context.annotation.Import;
import thingverse.discovery.consul.config.ConsulRegistrationImportSelector;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConsulRegistrationImportSelector.class)
public @interface EnableConsulRegistration {
}
