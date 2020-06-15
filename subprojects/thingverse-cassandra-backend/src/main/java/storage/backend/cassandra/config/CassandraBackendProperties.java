package storage.backend.cassandra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("thingverse.storage.backend.cassandra")
public class CassandraBackendProperties {

    private static final String DEFAULT_PATH = "build/cassandra-db";
    private static final int DEFAULT_PORT = 9042;
    private static final String DEFAULT_CONFIG_FILE = "test-embedded-cassandra.yaml";
    private static final String DEFAULT_BIND_ADDRESS = "127.0.0.1";

    /**
     * Switch to turn on/off this auto-configuration.
     */
    private boolean enabled = true;
    /**
     * The path at which an embedded Cassandra instance, if requested, will dump its files.
     */
    private String path = DEFAULT_PATH;
    /**
     * The port at which Cassandra listen for traffic.
     */
    private int port = DEFAULT_PORT;
    /**
     * Switch to make the embedded Cassandra instance run in the background.
     */
    private boolean background = false;
    /**
     * Switch to indicate that embedded Cassandra will start as a clean instance with no data.
     */
    private boolean startClean = false;
    /**
     * Switch to trigger instantiation of an embedded Cassandra instance.
     */
    private boolean embedded = false;
    /**
     * The config file, an embedded Cassandra will use. This file should be a classpath resource.
     */
    private String configFile = DEFAULT_CONFIG_FILE;

    private String address = DEFAULT_BIND_ADDRESS;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public boolean isStartClean() {
        return startClean;
    }

    public void setStartClean(boolean startClean) {
        this.startClean = startClean;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
