package com.thingverse.storage.backend.test;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = {"classpath:application-test.properties"})
public abstract class AbstractTest {
    protected static String SUCCESS_CHAR = "✓ ";
    protected static String FAILURE_CHAR = "✕ ";
    protected static String RUNNING_CHAR = "\uD83C\uDFC3 ";
}
