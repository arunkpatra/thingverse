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

package com.thingverse.api.encoder;

import com.thingverse.api.AbstractTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordEncoderTest extends AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordEncoderTest.class);

    @Autowired
    PasswordEncoder encoder;

    @Test
    public void testGoodPassword() {
        assertNotNull(encoder, "Encoder can not be null.");
        String result = encoder.encode("thingverse");
        assertFalse(result.equals("thingverse"));
        assertTrue(encoder.matches("thingverse", result));
        LOGGER.info("Password= thingverse, Encoded= " + result);
    }
}
