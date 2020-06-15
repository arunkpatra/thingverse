package com.thingverse.common.env.health;

import org.springframework.beans.factory.annotation.Value;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Value("${thingverse.app.health.status:true}")
public @interface EnvironmentHealth {
}
