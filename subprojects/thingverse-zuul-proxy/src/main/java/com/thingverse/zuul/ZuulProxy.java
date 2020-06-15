package com.thingverse.zuul;

import com.thingverse.ribbon.RibbonConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
@RibbonClients(defaultConfiguration = RibbonConfiguration.class)
public class ZuulProxy {

    public static void main(String[] args) {
        SpringApplication.run(ZuulProxy.class, args);
    }
}
