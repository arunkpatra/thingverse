package com.thingverse.tracing;

import akka.grpc.internal.GrpcMetadataImpl;
import akka.grpc.internal.JavaMetadataImpl;
import akka.grpc.javadsl.Metadata;
import com.thingverse.tracing.service.DummyParentService;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import thingverse.tracing.config.ThingverseTracer;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class ThingverseTracingTestAppTests extends AbstractTest {

    private static Logger LOGGER = LoggerFactory.getLogger(ThingverseTracingTestAppTests.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DummyParentService dummyParentService;

    @Autowired
    private ThingverseTracer thingverseTracer;

    @Test
    public void contextLoads() {
        validateBeanExistence(ThingverseTracer.class);
        ThingverseTracer tracerWrapper = context.getBean(ThingverseTracer.class);
        if (thingverseTracer.enabled) {
            Assert.assertNotNull(FAILURE_CHAR + "Was expecting non null tracer", tracerWrapper.tracer);
        } else {
            Assert.assertNull(FAILURE_CHAR + "Was not expecting non null tracer", tracerWrapper.tracer);
        }
        LOGGER.info(SUCCESS_CHAR + "Found Tracer.");
    }

    @Test
    public void testLocalNestedSpansTest() {
        LOGGER.info(RUNNING_CHAR + "Starting testLocalNestedSpansTest.");
        Metadata metadata = (new JavaMetadataImpl(new GrpcMetadataImpl(new io.grpc.Metadata())));
        String tracedResult = dummyParentService.someParentMethod();
        Assert.assertEquals(FAILURE_CHAR + "Did not receive expected result",
                "Hello World from Parent -> Hello World from Child", tracedResult);
        LOGGER.info(SUCCESS_CHAR + "Result was traced");
    }

    @Test
    public void testPropagatedNestedSpansTest() {
        LOGGER.info(RUNNING_CHAR + "Starting testPropagatedNestedSpansTest.");
        Metadata metadata = (new JavaMetadataImpl(new GrpcMetadataImpl(getPropagatedTestMetaData())));
        String tracedResult = dummyParentService.someParentMethodWithMetadata(metadata);
        Assert.assertEquals(FAILURE_CHAR + "Did not receive expected result",
                "Hello World from Parent -> Hello World from Child", tracedResult);
        LOGGER.info(SUCCESS_CHAR + "Result was traced");
    }

    @Test
    public void someParentMethodFutureTest() throws Exception{
        String res = dummyParentService.someParentMethodFuture().handle((r, t) -> r).toCompletableFuture().get();
        Assert.assertEquals(res, "Hello World from future parent");
    }

    @Test(expected = ExecutionException.class)
    public void someParentMethodWithMetadataWithExceptionTest() throws Exception {
        Metadata metadata = (new JavaMetadataImpl(new GrpcMetadataImpl(getPropagatedTestMetaData())));
        dummyParentService.someParentMethodWithMetadataWithException(metadata);
    }
    @Test
    public void someParentMethodFutureException() {
        dummyParentService.someParentMethodFutureException();
    }
    @Test(expected = ExecutionException.class)
    public void someParentMethodWithMetadata2Test() throws Exception {
        Metadata metadata = (new JavaMetadataImpl(new GrpcMetadataImpl(getPropagatedTestMetaData())));
        dummyParentService.someParentMethodWithMetadata2(metadata);
    }
    @Test
    public void testPropagatedNestedSpansWithInvalidMetadataTest() {
        LOGGER.info(RUNNING_CHAR + "Starting testPropagatedNestedSpansWithInvalidMetadataTest.");
        Metadata metadata = (new JavaMetadataImpl(new GrpcMetadataImpl(getInvalidPropagatedTestMetaData())));
        String tracedResult = dummyParentService.someParentMethodWithMetadata(metadata);
        Assert.assertEquals(FAILURE_CHAR + "Did not receive expected result",
                "Hello World from Parent -> Hello World from Child", tracedResult);
        LOGGER.info(SUCCESS_CHAR + "Result was traced");
    }

    private void validateBeanExistence(Class<?>... types) {
        Arrays.stream(types).forEach(t -> {
            if (context.getBeanNamesForType(t).length == 0) {
                Assert.fail(String.format("Bean of type %s was not found", t.getSimpleName()));
            }
        });
    }

    private io.grpc.Metadata getPropagatedTestMetaData() {
        io.grpc.Metadata m = new io.grpc.Metadata();
        m.put(io.grpc.Metadata.Key.of("x-b3-spanid", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "c9f621954448e21e");
        m.put(io.grpc.Metadata.Key.of("x-b3-parentspanid", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "6aa95a61699d289e");
        m.put(io.grpc.Metadata.Key.of("x-b3-sampled", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "1");
        m.put(io.grpc.Metadata.Key.of("x-b3-traceid", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "6aa95a61699d289e");
        return m;
    }

    private io.grpc.Metadata getInvalidPropagatedTestMetaData() {
        io.grpc.Metadata m = new io.grpc.Metadata();
        m.put(io.grpc.Metadata.Key.of("junk-x-b3-spanid", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "c9f621954448e21e");
        m.put(io.grpc.Metadata.Key.of("junk-x-b3-parentspanid", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "6aa95a61699d289e");
        m.put(io.grpc.Metadata.Key.of("junk-x-b3-sampled", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "1");
        m.put(io.grpc.Metadata.Key.of("junk-x-b3-traceid", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "6aa95a61699d289e");
        return m;
    }
}
