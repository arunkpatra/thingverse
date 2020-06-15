package com.thingverse.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import com.thingverse.ribbon.rule.StickySessionRule;
import org.springframework.context.annotation.Bean;

public class RibbonConfiguration {
    @Bean
    public IRule ribbonRule(final IClientConfig config) {
        return new StickySessionRule();
    }
}
