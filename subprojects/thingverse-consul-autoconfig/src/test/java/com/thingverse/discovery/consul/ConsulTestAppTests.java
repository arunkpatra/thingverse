package com.thingverse.discovery.consul;

import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import com.thingverse.common.env.health.HealthChecker;
import com.thingverse.common.env.health.HealthStatus;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import thingverse.discovery.consul.config.ConsulRegistrationProperties;
import thingverse.discovery.consul.health.ConsulHealthChecker;
import thingverse.discovery.consul.service.ConsulRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsulTestAppTests extends AbstractTest {

    private static Logger LOGGER = LoggerFactory.getLogger(ConsulTestAppTests.class);

    @Autowired
    private ConsulRegistrar consulRegistrar;
    @Autowired
    private ConsulRegistrationProperties properties;
    @Autowired
    private InetUtils inetUtils;

    @Autowired
    private ConfigurableEnvironment configurableEnvironment;

    @Test
    public void contextLoads() {

    }

    @Test
    public void consulHealthCheckTest() {
        HealthChecker healthChecker = new ConsulHealthChecker();
        DeferredLog logger = new DeferredLog();
        Map<String, Object> params = new HashMap<>();
        params.put("consul-host", properties.getHost());
        params.put("consul-port", properties.getPort());
        HealthChecker.CheckResult result = healthChecker.checkHealth(configurableEnvironment, params, logger);
        Assert.assertEquals(FAILURE_CHAR + "Was expecting UP status", HealthStatus.UP, result.status);
        LOGGER.info(SUCCESS_CHAR + "Consul health checker reported UP status.");
    }

    @Test
    public void queryHealthyServiceTest() {
        LOGGER.info("Querying healthy services...");
        HealthServicesRequest request = HealthServicesRequest.newBuilder()
                .setPassing(true)
                .setQueryParams(QueryParams.DEFAULT)
                .build();
        Response<List<HealthService>> healthyServices = consulRegistrar.getClient().getHealthServices(properties.getServiceName(), request);
        for (HealthService h : healthyServices.getValue()) {
            LOGGER.info("Service : {}", h);
        }
    }

    @Test
    public void registrationTest() {
        ConsulRegistrationProperties props = consulRegistrar.getProperties();
        Map<String, String> metaData = new HashMap<>();
        metaData.put("app_name", "Thingverse");
        metaData.put("version", "1.0.0");

        ConsulRegistrar.ServiceCheckSettings checkSettings = ConsulRegistrar.ServiceCheckSettings
                .create()
                .withGrpc(inetUtils.findFirstNonLoopbackHostInfo().getIpAddress().concat(":").concat(Integer.toString(9999)))
                .withGrpcUseTls(false)
                .withInitialStatus(props.getServiceInitialStatus())
                .withInterval(props.getServiceCheckInterval());

        ConsulRegistrar.ServiceRegistrationSettings settings = ConsulRegistrar.ServiceRegistrationSettings
                .create()
                .withServiceName(props.getServiceName())
                .withId(props.getServiceId())
                .withPort(9999)
                .withAddress(inetUtils.findFirstNonLoopbackHostInfo().getIpAddress())
                .withCheckSettings(checkSettings)
                .withMetaData(metaData)
                .withTags(getTagList());

        Response<Void> response = consulRegistrar.register(settings);

        LOGGER.info("Registered new service with these settings: {}. Response was {}", settings, response);
    }

    private List<String> getTagList() {
        List<String> tagList = new ArrayList<>();
        tagList.add("system:".concat("test-service-name"));
        tagList.add("akka-management-port:".concat(Integer.toString(8585)));
        tagList.add("grpc-service-port:".concat(Integer.toString(9999)));
        return tagList;
    }
}
