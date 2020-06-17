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

package thingverse.discovery.consul.service;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thingverse.discovery.consul.config.ConsulRegistrationProperties;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

public class ConsulRegistrarImpl implements ConsulRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulRegistrarImpl.class);
    private final ConsulRegistrationProperties properties;
    private final ConsulClient consulClient;
    private final Map<String, String> serviceIdMap = new HashMap<>(); // key = serviceId, value = service name

    public ConsulRegistrarImpl(ConsulRegistrationProperties properties) {
        this.properties = properties;
        this.consulClient = properties.isEnabled() ? new ConsulClient(properties.getHost(), properties.getPort()) : null;
    }

    public ConsulRegistrationProperties getProperties() {
        return properties;
    }

    @Override
    public ConsulClient getClient() {
        if (null == consulClient) {
            throw new IllegalStateException("A client yas not been configured yet.");
        }
        return this.consulClient;
    }

    @Override
    public Response<Void> register(ServiceRegistrationSettings settings) {
        if (null == this.consulClient) {
            LOGGER.error("Can't yet register services with Consul. " +
                    "Maybe thingverse.consul.registration.enabled=true is needed to activate registration.");
            return null;
        }
        //LOGGER.info("Registering service {} with Consul with these settings: ", settings);
        NewService service = new NewService();
        service.setName(settings.getServiceName());
        service.setId(settings.getId());
        service.setAddress(settings.getAddress());
        service.setPort(settings.getPort());
        service.setTags(settings.getTags());
        service.setMeta(settings.getMetaData());

        NewService.Check check = new NewService.Check();
        check.setGrpc(settings.getCheckSettings().getGrpc());
        check.setGrpcUseTLS(settings.getCheckSettings().isGrpcUseTls());
        check.setInterval(settings.getCheckSettings().getInterval());
        check.setStatus(settings.getCheckSettings().getInitialStatus());
        check.setDeregisterCriticalServiceAfter(settings.getCheckSettings().getHealthCheckCriticalTimeout());
        service.setCheck(check);
        LOGGER.debug("Registering service with consul: {}", service);
        Response<Void> response = consulClient.agentServiceRegister(service);
        serviceIdMap.put(service.getId(), service.getName());

        return response;
    }

    @Override
    public void deRegister(String serviceId) {
        if (null == this.consulClient) {
            LOGGER.error("Can't yet de-register services with Consul. " +
                    "Maybe thingverse.consul.registration.enabled=true is needed to activate registration.");
            return;
        }
        if (serviceIdMap.containsKey(serviceId)) {
            this.consulClient.agentServiceDeregister(serviceId);
            LOGGER.info("De-registered service with Id {}.", serviceId);
        } else {
            LOGGER.error("Service instance {} was not found in registry", serviceId);
        }
    }

    @Override
    public void deRegisterAll() {
        if (null == this.consulClient) {
            return;
        }
        serviceIdMap.forEach((sid, sn) -> deRegister(sid));
    }

    @PreDestroy
    public void windUp() {
        try {
            deRegisterAll();
        } catch (Throwable t) {
            LOGGER.error("Error while de-registering services: {}", t.getMessage());
        }

    }
}
