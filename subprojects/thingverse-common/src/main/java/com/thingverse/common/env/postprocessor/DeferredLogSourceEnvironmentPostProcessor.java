package com.thingverse.common.env.postprocessor;

import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;

public interface DeferredLogSourceEnvironmentPostProcessor extends EnvironmentPostProcessor {

    void switchToImmediateLogger();

    DeferredLog getLogger();
}
