package com.thingverse.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import thingverse.discovery.consul.annotation.EnableConsulRegistration;

@EnableConsulRegistration
@Configuration
@Profile("embedded-consul")
public class ConsulConfigEmbedded {
}
