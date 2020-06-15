package com.thingverse.grpc.client;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = {"classpath:application-test.properties"})
public abstract class AbstractTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);
    protected static String SUCCESS_CHAR = "✓ ";
    protected static String FAILURE_CHAR = "✕ ";
    protected static String RUNNING_CHAR = "\uD83C\uDFC3 ";

}
