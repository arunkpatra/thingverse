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

package com.thingverse.api.security.config;

import com.thingverse.api.config.ThingverseApiProperties;
import com.thingverse.api.services.UserService;
import com.thingverse.common.env.health.ResourcesHealthyCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ThingverseApiProperties.class)
@Conditional({ResourcesHealthyCondition.class})
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);
    private final ThingverseApiProperties properties;

    private final UserDetailsService jwtUserDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    public WebSecurityConfig(UserDetailsService jwtUserDetailsService,
                             JwtRequestFilter jwtRequestFilter,
                             ThingverseApiProperties properties) {

        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.properties = properties;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (properties.isSecured()) {
            LOGGER.info("Enabling HTTP Security on '/api/**' endpoints. Only authenticated users with 'ROLE_USER' " +
                    "can access '/api/**' endpoints on this server.");
            //@formatter:off
            http
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/assets/**").permitAll()
                    .antMatchers("/instances").permitAll()
                    .antMatchers("/instances/*").permitAll()
                    .antMatchers("/actuator/**").permitAll()
                    .antMatchers("/auth/login").permitAll()
                    .antMatchers("/swagger**").permitAll()
                    .antMatchers("/v2/api-docs/**").permitAll()
                    .antMatchers("/swagger-resources/configuration/**").permitAll()
                    .antMatchers("/webjars/**").permitAll()
                    .antMatchers("/api/**").authenticated()
                    .antMatchers("/api/**").hasAnyRole("USER")
                    .anyRequest().authenticated().and()
                    .exceptionHandling()
                    .authenticationEntryPoint(new JwtAuthenticationEntryPoint()).and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            //@formatter:on
            // Add a filter to validate the tokens with every request
            http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            LOGGER.info("HTTP Security is disabled on all endpoints. If you want to enable security, pass the " +
                    "'-Dthingverse.api.secured=true' property.");
            //@formatter:off
            http
                    .csrf().disable()
                    .authorizeRequests()
                    .anyRequest().permitAll()
                    .and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            //@formatter:on
        }
    }

    @Configuration
    @Conditional({ResourcesHealthyCondition.class})
    public static class SecurityConfigInner {
        @Bean
        public JwtUserDetailsService jwtUserDetailsService(UserService userService) {
            return new JwtUserDetailsService(userService);
        }

        @Bean
        public JwtRequestFilter jwtRequestFilter(JwtUserDetailsService jwtUserDetailsService, JwtTokenUtil jwtTokenUtil) {
            return new JwtRequestFilter(jwtUserDetailsService, jwtTokenUtil);
        }

        @Bean
        public JwtTokenUtil jwtTokenUtil(ThingverseApiProperties properties) {
            return new JwtTokenUtil(properties);
        }
    }
}