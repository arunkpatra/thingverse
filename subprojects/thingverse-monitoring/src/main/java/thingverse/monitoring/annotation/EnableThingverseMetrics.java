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

package thingverse.monitoring.annotation;

import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import thingverse.monitoring.config.ThingverseMetricsImportSelector;

import java.lang.annotation.*;

/**
 * This configures the metrics collection infrastructure for Thingverse.
 *
 * @author Arun Patra
 * @see MetricsCollector
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(ThingverseMetricsImportSelector.class)
public @interface EnableThingverseMetrics {
}
