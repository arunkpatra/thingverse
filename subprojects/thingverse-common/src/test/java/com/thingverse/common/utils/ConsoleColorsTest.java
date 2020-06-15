package com.thingverse.common.utils;

import org.junit.Test;
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
