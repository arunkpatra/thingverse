package thingverse.discovery.consul.health;

import com.thingverse.common.env.health.HealthChecker;
import com.thingverse.common.env.health.HealthStatus;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

public class ConsulHealthChecker implements HealthChecker {

    private boolean isReachable(String host, int port, int timeout, DeferredLog logger) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            logger.debug(String.format("Connection successful with %s:%d in %d milli seconds.", host, port, timeout));
            return true;
        } catch (IOException e) {
            logger.debug(String.format("Connection failed with %s:%d in %d milli seconds. Giving up.", host, port, timeout));
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    @Override
    public CheckResult checkHealth(ConfigurableEnvironment env, Map<String, Object> params, DeferredLog logger) {
        // Check Consul reachability
        String host = (String) params.getOrDefault("consul-host", "localhost");
        int port = (int) params.getOrDefault("consul-port", 8500);
        return new CheckResult(this.getClass().getName(),
                isReachable(host, port, 5000, logger) ? HealthStatus.UP : HealthStatus.DOWN, this.getResourceType());
    }

    @Override
    public String getResourceType() {
        return "CONSUL";
    }
}
