/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.thingverse.common.log;

import com.thingverse.common.env.postprocessor.DeferredLogSourceEnvironmentPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.Entry.comparingByKey;

/**
 * A bean of this type, must be configured in an application context, with a high precedence.
 *
 * @author Arun Patra
 */
public class DeferredLogActivatorImpl implements DeferredLogActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeferredLogActivatorImpl.class);
    public final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 1;
    Map<Integer, DeferredLogSourceEnvironmentPostProcessor> beanMap = new HashMap<>();

    public DeferredLogActivatorImpl(ApplicationContext context) {
        String[] beans = context.getBeanNamesForType(DeferredLogSourceEnvironmentPostProcessor.class);
        LOGGER.info("Found these DeferredLogSource(s): {}, will activate their loggers now.", Arrays.toString(beans));
        for (String b : beans) {
            extractBeanOrder(b, context);
        }
        beanMap.entrySet().stream().sorted(comparingByKey()).forEach(e -> {
            LOGGER.info("Switching logger on {}, it's order was {}", e.getValue().getClass().getSimpleName(), e.getKey());
            e.getValue().switchToImmediateLogger();
        });
    }

    private void extractBeanOrder(String beanName, ApplicationContext context) {
        Object bean = context.getBean(beanName);
        Order order = bean.getClass().getAnnotation(Order.class);
        if (null != order) {
            beanMap.put(order.value(), (DeferredLogSourceEnvironmentPostProcessor) bean);
        } else {
            if (bean instanceof Ordered) {
                beanMap.put(((Ordered) bean).getOrder(), (DeferredLogSourceEnvironmentPostProcessor) bean);
            } else {
                LOGGER.warn("Can not determine order of bean. You must specify order using the @Order annotation " +
                        "or implement the Ordered interface. Logger will not be activated for this bean.");
            }
        }
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
