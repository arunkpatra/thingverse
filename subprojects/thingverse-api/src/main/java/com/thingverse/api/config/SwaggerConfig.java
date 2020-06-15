package com.thingverse.api.config;


import com.thingverse.common.env.health.ResourcesHealthyCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableSwagger2
//@EnableWebMvc
@Conditional({ResourcesHealthyCondition.class})
public class SwaggerConfig implements WebMvcConfigurer {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.thingverse.api.controllers"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(new ArrayList<SecurityScheme>(Collections.singletonList(apiKey())))
                .securityContexts(new ArrayList<SecurityContext>(Collections.singletonList(securityContext())))
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Thingverse REST API Documentation",
                "REST APIs for Thingverse. These APIs expose capability to interact with things in Thingverse.",
                "0.0.1",
                "Terms of service",
                new Contact("Thingverse", "www.thingverse.com", "info@thingverse.com"),
                "License of API", "API license URL", Collections.emptyList());
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addViewControllers (ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/swagger-ui.html");
    }

    private ApiKey apiKey() {
        return new ApiKey("Access Token", "Authorization", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("/api.*"))
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return new ArrayList<SecurityReference>(
                Collections.singletonList(new SecurityReference("Access Token", authorizationScopes)));
    }
}
