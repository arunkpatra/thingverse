package thingverse.discovery.consul.service;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import thingverse.discovery.consul.config.ConsulRegistrationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ConsulRegistrar {

    Response<Void> register(ServiceRegistrationSettings settings);

    void deRegister(String serviceId);

    void deRegisterAll();

    ConsulRegistrationProperties getProperties();

    ConsulClient getClient();

    class ServiceRegistrationSettings {
        private String serviceName = "";
        private String id = "";
        private String address = "";
        private int port;
        private Map<String, String> metaData = new HashMap<>();
        private List<String> tags = new ArrayList<>();
        private ServiceCheckSettings checkSettings = ServiceCheckSettings.create();

        // private
        private ServiceRegistrationSettings() {
        }

        static public ServiceRegistrationSettings create() {
            return new ServiceRegistrationSettings();
        }

        public ServiceRegistrationSettings withServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public ServiceRegistrationSettings withId(String id) {
            this.id = id;
            return this;
        }

        public ServiceRegistrationSettings withAddress(String address) {
            this.address = address;
            return this;
        }

        public ServiceRegistrationSettings withPort(int port) {
            this.port = port;
            return this;
        }

        public ServiceRegistrationSettings withMetaData(Map<String, String> metaData) {
            this.metaData = metaData;
            return this;
        }

        public ServiceRegistrationSettings withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public ServiceRegistrationSettings withCheckSettings(ServiceCheckSettings checkSettings) {
            this.checkSettings = checkSettings;
            return this;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getId() {
            return id;
        }

        public String getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public Map<String, String> getMetaData() {
            return metaData;
        }

        public List<String> getTags() {
            return tags;
        }

        public ServiceCheckSettings getCheckSettings() {
            return checkSettings;
        }

        @Override
        public String toString() {
            return "ServiceRegistrationSettings{" +
                    "serviceName='" + serviceName + '\'' +
                    ", id='" + id + '\'' +
                    ", address='" + address + '\'' +
                    ", port=" + port +
                    ", metaData=" + metaData +
                    ", tags=" + tags +
                    ", checkSettings=" + checkSettings +
                    '}';
        }
    }

    class ServiceCheckSettings {
        private String grpc = "";
        private boolean grpcUseTls = false;
        private String interval = "3s";
        private String initialStatus = "critical";
        private String healthCheckCriticalTimeout = "90s";

        // private
        private ServiceCheckSettings() {
        }

        static public ServiceCheckSettings create() {
            return new ServiceCheckSettings();
        }

        public ServiceCheckSettings withGrpc(String grpc) {
            this.grpc = grpc;
            return this;
        }

        public ServiceCheckSettings withGrpcUseTls(boolean grpcUseTls) {
            this.grpcUseTls = grpcUseTls;
            return this;
        }

        public ServiceCheckSettings withInterval(String interval) {
            this.interval = interval;
            return this;
        }

        public ServiceCheckSettings withInitialStatus(String initialStatus) {
            this.initialStatus = initialStatus;
            return this;
        }

        public ServiceCheckSettings withHealthCheckCriticalTimeout(String healthCheckCriticalTimeout) {
            this.healthCheckCriticalTimeout = healthCheckCriticalTimeout;
            return this;
        }

        public String getGrpc() {
            return grpc;
        }

        public boolean isGrpcUseTls() {
            return grpcUseTls;
        }

        public String getInterval() {
            return interval;
        }

        public String getInitialStatus() {
            return initialStatus;
        }

        public String getHealthCheckCriticalTimeout() {
            return healthCheckCriticalTimeout;
        }

        @Override
        public String toString() {
            return "ServiceCheckSettings{" +
                    "grpc='" + grpc + '\'' +
                    ", grpcUseTls=" + grpcUseTls +
                    ", interval='" + interval + '\'' +
                    ", initialStatus='" + initialStatus + '\'' +
                    '}';
        }
    }
}
