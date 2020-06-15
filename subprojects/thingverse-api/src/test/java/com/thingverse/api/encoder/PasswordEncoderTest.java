package com.thingverse.api.encoder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.thingverse.api.AbstractTest;

import static org.junit.Assert.*;

public class PasswordEncoderTest extends AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordEncoderTest.class);

    @Autowired
    PasswordEncoder encoder;

    @Test
    public void testGoodPassword() {
        assertNotNull("Encoder can not be null.", encoder);
        String result = encoder.encode("thingverse");
        assertFalse(result.equals("thingverse"));
        assertTrue(encoder.matches("thingverse", result));
        LOGGER.info("Password= thingverse, Encoded= " + result);
    }
}
