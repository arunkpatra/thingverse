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

package thingverse.resilience.core.annotations;

import org.springframework.context.annotation.Import;
import thingverse.resilience.core.config.ThingverseResilienceImportSelector;
import thingverse.resilience.core.config.ThingverseResilienceProperties;

import java.lang.annotation.*;

/**
 * Use this annotation on any SpringBoot Application class to enable a cassandra based akka storage backend.
 * <p>
 * See {@link ThingverseResilienceProperties}.
 * <p>
 * You can provide properties to override defaults, e.g.
 * <p>
 * thingverse.storage.backend.cassandra.port=9999
 * thingverse.storage.backend.cassandra.path=target/path
 * <p>
 * Enable or disable using the thingverse.storage.backend.cassandra.enabled property.
 * </p>
 *
 * @author Arun Patra
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ThingverseResilienceImportSelector.class)
public @interface EnableThingverseResilience {

}
