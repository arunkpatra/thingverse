package com.thingverse.common.env.health;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ResourcesHealthyCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition("ResourcesHealthyCondition");
        boolean allResourcesAreUp = context.getEnvironment()
                .getProperty("thingverse.app.health.status", Boolean.class, true);
        return allResourcesAreUp ? ConditionOutcome.match(message.available("All required resources")) :
                ConditionOutcome.noMatch(message.because("Some resources are DOWN"));
    }
}
