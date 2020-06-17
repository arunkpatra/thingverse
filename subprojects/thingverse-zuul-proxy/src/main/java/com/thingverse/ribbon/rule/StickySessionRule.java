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

package com.thingverse.ribbon.rule;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StickySessionRule extends ZoneAvoidanceRule {

    public static final String COOKIE_NAME_SUFFIX = "-" + StickySessionRule.class.getSimpleName();

    @Override
    public Server choose(Object key) {
        Optional<Cookie> cookie = getCookie(key);

        if (cookie.isPresent()) {
            Cookie hash = cookie.get();
            List<Server> servers = getLoadBalancer().getReachableServers();
            Optional<Server> server = servers.stream()
                    .filter(s -> s.isAlive() && s.isReadyToServe())
                    .filter(s -> hash.getValue().equals("" + s.hashCode()))
                    .findFirst();

            if (server.isPresent()) {
                return server.get();
            }
        }

        return useNewServer(key);
    }

    private Server useNewServer(Object key) {
        Server server = super.choose(key);
        HttpServletResponse response = RequestContext.getCurrentContext().getResponse();
        if (response != null) {
            String cookieName = getCookieName(server);
            Cookie newCookie = new Cookie(cookieName, "" + server.hashCode());
            newCookie.setPath("/");
            response.addCookie(newCookie);
        }
        return server;
    }

    private Optional<Cookie> getCookie(Object key) {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        if (request != null) {
            Server server = super.choose(key);
            String cookieName = getCookieName(server);
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                return Arrays.stream(cookies)
                        .filter(c -> c.getName().equals(cookieName))
                        .findFirst();
            }
        }

        return Optional.empty();
    }

    private String getCookieName(Server server) {
        return server.getMetaInfo().getAppName() + COOKIE_NAME_SUFFIX;
    }
}
