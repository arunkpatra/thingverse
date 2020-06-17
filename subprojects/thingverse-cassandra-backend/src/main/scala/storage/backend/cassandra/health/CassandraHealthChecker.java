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

package storage.backend.cassandra.health;

import com.thingverse.common.env.health.HealthChecker;
import com.thingverse.common.env.health.HealthStatus;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;

public class CassandraHealthChecker implements HealthChecker {

    public static boolean isReachable(String host, int port, int timeout, DeferredLog logger) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            logger.debug(String.format("Contact successful with %s:%d in %d milli seconds.", host, port, timeout));
            return true;
        } catch (IOException e) {
            logger.debug(String.format("Connection failed with %s:%d in %d milli seconds. Giving up.", host, port, timeout));
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    @Override
    public CheckResult checkHealth(ConfigurableEnvironment env, Map<String, Object> properties, DeferredLog logger) {
        // Check Cassandra reachability
        String[] contactPoints = (String[]) properties.get("cassandra-contact-points");

        boolean anyContactPointsUnreachable = Arrays.stream(contactPoints).map(c -> {
            String newString = c.replace("\"", "");
            StringTokenizer st = new StringTokenizer(newString, ":");
            if (st.countTokens() == 2) {
                String host = st.nextToken();
                String port = st.nextToken();
                return isReachable(host, Integer.parseInt(port), 5000, logger);
            } else {
                return false;
            }
        }).anyMatch(r -> !r);
        return new CheckResult(this.getClass().getName(),
                anyContactPointsUnreachable ? HealthStatus.DOWN : HealthStatus.UP, this.getResourceType());
    }

    @Override
    public String getResourceType() {
        return "CASSANDRA";
    }

}
