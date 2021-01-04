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

package com.thingverse.common.utils;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.thingverse.common.utils.ConsoleColors.*;

public class ConsoleColorsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleColorsTest.class);

    @Test
    public void testBlueFormatting() {
        LOGGER.info("Hello {}", format(BLUE_BOLD, "Blue!"));
        LOGGER.info("Hello {}", thanos("Thanos!"));
        LOGGER.info("Hello {}", ironman("Tony!"));
        LOGGER.info("Hello {}", hulk("Bruce!"));
    }
}
