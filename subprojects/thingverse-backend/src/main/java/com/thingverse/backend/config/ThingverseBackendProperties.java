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

package com.thingverse.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;

@ConfigurationProperties("thingverse.backend")
public class ThingverseBackendProperties {

    public static final String DEFAULT_GRPC_SERVER_HOST = "0.0.0.0";
    public static final int DEFAULT_GRPC_SERVER_PORT = 8080;
    public static final int DEFAULT_GRPC_SERVER_PORT_HTTPS = 8443;
    public static final int DEFAULT_AKKA_REMOTE_PORT = 2551;
    public static final int DEFAULT_AKKA_MANAGEMENT_HTTP_PORT = 8558;
    public static final String DEFAULT_ACTOR_TIMEOUT_DURATION = "7200s";
    public static final int DEFAULT_EVENTS_BEFORE_SNAPSHOTTING = 10000;
    public static final int DEFAULT_SNAPSHOTS_TO_KEEP = 2;
    public static final String DEFAULT_OPERATION_TIMEOUT_DURATION = "20s";
    public static final String DEFAULT_ACTOR_SYSTEM_NAME = "Thingverse";
    public static final String DEFAULT_KEYSTORE_FILE_NAME = "localhost.p12";
    public static final String DEFAULT_KEYSTORE_PASSWORD = "junk-password";
    public static final String[] DEFAULT_BACKEND_ROLES = {"read-model", "write-model"};
    public static final String DEFAULT_BACKEND_AKKA_CLUSTER_CONFIG_FILE = "cluster-mode-akka-backend.conf";
    public static final String DEFAULT_BACKEND_AKKA_STANDALONE_CONFIG_FILE = "application.conf";
    public static final int DEFAULT_AKKA_MGMT_PORT_MAX = 59000;
    public static final String DEFAULT_BACKEND_AKKA_CONFIG_FILE = DEFAULT_BACKEND_AKKA_STANDALONE_CONFIG_FILE;
    public static final String[] DEFAULT_CASSANDRA_CONTACT_POINTS = {"127.0.0.1\":\"9043"};
    public static final String DEFAULT_CASSANDRA_LOCAL_DATACENTER = "datacenter1";
    //private static final String[] DEFAULT_CASSANDRA_CONTACT_POINTS = {};
    public static final int DEFAULT_AKKA_MGMT_PORT_MIN = 50000;

    /**
     * Enable HTTPS server.
     */
    public boolean httpsServerEnabled = false;

    /**
     * Whether all certificates will be trusted by server.
     */
    private boolean insecureMode = false;
    /**
     * HTTPS Server port.
     */
    private int httpsServerPort = 8443;
    /**
     * The keystore file name to look for in classpath. Should be in .p12 format.
     */
    private String keyStoreFileName = DEFAULT_KEYSTORE_FILE_NAME;

    /**
     * The keystore password.
     */
    private String keyStorePassword = DEFAULT_KEYSTORE_PASSWORD;
    private String backendAkkaConfigFile = "";
    /**
     * The CQRS roles this backend node will have.
     */
    private String[] roles = DEFAULT_BACKEND_ROLES;
    /**
     * The Cassandra contact points to use.
     */
    private String[] cassandraContactPoints = DEFAULT_CASSANDRA_CONTACT_POINTS;
    /**
     * Name of the Cassandra local datacenter.
     */
    private String cassandraLocalDatacenter = DEFAULT_CASSANDRA_LOCAL_DATACENTER;
    /**
     * Switch to activate dumping of the entire config during startup that Akka uses to start up the actor system.
     */
    private boolean dumpActorSystemConfig = false;
    /**
     * Print the initial actor system tree during startup.
     */
    private boolean printInitialActorSystemTree = false;
    /**
     * IP address to which the gRPC server will bind to.
     */
    private String grpcServerHost = DEFAULT_GRPC_SERVER_HOST;
    /**
     * Port at which the gRPC server is hosted.
     */
    private int grpcServerPort = DEFAULT_GRPC_SERVER_PORT;

    /**
     * Port at which the gRPC server is hosted.
     */
    private int grpcServerPortHttps = DEFAULT_GRPC_SERVER_PORT_HTTPS;

    /**
     * Akka remote port for this node in the cluster.
     */
    private int akkaRemotePort = DEFAULT_AKKA_REMOTE_PORT;
    /**
     * Port at which the Akka management server will listen.
     */
    private int akkaManagementHttpPort = DEFAULT_AKKA_MANAGEMENT_HTTP_PORT;
    /**
     * Duration after which an actor will be automatically passivated.
     */
    private String actorTimeoutDuration = DEFAULT_ACTOR_TIMEOUT_DURATION;
    /**
     * Allowed events before which a snapshot is taken on an actor.
     */
    private int eventsBeforeSnapshotting = DEFAULT_EVENTS_BEFORE_SNAPSHOTTING;
    /**
     * Maximum number of snapshots to preserve.
     */
    private int maxSnapshotsToKeep = DEFAULT_SNAPSHOTS_TO_KEEP;
    /**
     * Duration after which async operations will time out.
     */
    private String operationTimeoutDuration = DEFAULT_OPERATION_TIMEOUT_DURATION;
    /**
     * The mode in which the node will operate.
     */
    private BackendOperationMode backendOperationMode = BackendOperationMode.STANDALONE;

    /**
     * The service discovery method to be used for Cluster Bootstrap process.
     */
    private ClusterBootstrapServiceDiscoveryMethod clusterBootstrapServiceDiscoveryMethod = ClusterBootstrapServiceDiscoveryMethod.CONSUL;
    /**
     * For dynamic Akka management server port selection, the minimum port number to choose from.
     */
    private int akkaManagementHttpPortMin = DEFAULT_AKKA_MGMT_PORT_MIN;
    /**
     * For dynamic Akka management server port selection, the minimum port number to choose from.
     */
    private int akkaManagementHttpPortMax = DEFAULT_AKKA_MGMT_PORT_MAX;
    /**
     * The Actor system name.
     */
    private String actorSystemName = DEFAULT_ACTOR_SYSTEM_NAME;

    public ClusterBootstrapServiceDiscoveryMethod getClusterBootstrapServiceDiscoveryMethod() {
        return clusterBootstrapServiceDiscoveryMethod;
    }

    public void setClusterBootstrapServiceDiscoveryMethod(ClusterBootstrapServiceDiscoveryMethod clusterBootstrapServiceDiscoveryMethod) {
        this.clusterBootstrapServiceDiscoveryMethod = clusterBootstrapServiceDiscoveryMethod;
    }

    public int getGrpcServerPort() {
        return grpcServerPort;
    }

    public void setGrpcServerPort(int grpcServerPort) {
        this.grpcServerPort = grpcServerPort;
    }

    public int getAkkaRemotePort() {
        return akkaRemotePort;
    }

    public void setAkkaRemotePort(int akkaRemotePort) {
        this.akkaRemotePort = akkaRemotePort;
    }

    public int getAkkaManagementHttpPort() {
        return akkaManagementHttpPort;
    }

    public void setAkkaManagementHttpPort(int akkaManagementHttpPort) {
        this.akkaManagementHttpPort = akkaManagementHttpPort;
    }

    public String getActorTimeoutDuration() {
        return actorTimeoutDuration;
    }

    public void setActorTimeoutDuration(String actorTimeoutDuration) {
        this.actorTimeoutDuration = actorTimeoutDuration;
    }

    public int getEventsBeforeSnapshotting() {
        return eventsBeforeSnapshotting;
    }

    public void setEventsBeforeSnapshotting(int eventsBeforeSnapshotting) {
        this.eventsBeforeSnapshotting = eventsBeforeSnapshotting;
    }

    public int getMaxSnapshotsToKeep() {
        return maxSnapshotsToKeep;
    }

    public void setMaxSnapshotsToKeep(int maxSnapshotsToKeep) {
        this.maxSnapshotsToKeep = maxSnapshotsToKeep;
    }

    public String getOperationTimeoutDuration() {
        return operationTimeoutDuration;
    }

    public void setOperationTimeoutDuration(String operationTimeoutDuration) {
        this.operationTimeoutDuration = operationTimeoutDuration;
    }

    public BackendOperationMode getBackendOperationMode() {
        return backendOperationMode;
    }

    public void setBackendOperationMode(BackendOperationMode backendOperationMode) {
        this.backendOperationMode = backendOperationMode;
    }

    public int getAkkaManagementHttpPortMin() {
        return akkaManagementHttpPortMin;
    }

    public int getAkkaManagementHttpPortMax() {
        return akkaManagementHttpPortMax;
    }

    public String getActorSystemName() {
        return actorSystemName;
    }

    public void setActorSystemName(String actorSystemName) {
        this.actorSystemName = actorSystemName;
    }

    public String getGrpcServerHost() {
        return grpcServerHost;
    }

    public void setGrpcServerHost(String grpcServerHost) {
        this.grpcServerHost = grpcServerHost;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public boolean isDumpActorSystemConfig() {
        return dumpActorSystemConfig;
    }

    public void setDumpActorSystemConfig(boolean dumpActorSystemConfig) {
        this.dumpActorSystemConfig = dumpActorSystemConfig;
    }

    public boolean isPrintInitialActorSystemTree() {
        return printInitialActorSystemTree;
    }

    public void setPrintInitialActorSystemTree(boolean printInitialActorSystemTree) {
        this.printInitialActorSystemTree = printInitialActorSystemTree;
    }

    public String[] getCassandraContactPoints() {
        return cassandraContactPoints;
    }

    public void setCassandraContactPoints(String[] cassandraContactPoints) {
        this.cassandraContactPoints = cassandraContactPoints;
    }

    public String getBackendAkkaConfigFile() {
        return backendAkkaConfigFile;
    }

    public void setBackendAkkaConfigFile(String backendAkkaConfigFile) {
        this.backendAkkaConfigFile = backendAkkaConfigFile;
    }

    public String getCassandraLocalDatacenter() {
        return cassandraLocalDatacenter;
    }

    public void setCassandraLocalDatacenter(String cassandraLocalDatacenter) {
        this.cassandraLocalDatacenter = cassandraLocalDatacenter;
    }

    public int getGrpcServerPortHttps() {
        return grpcServerPortHttps;
    }

    public void setGrpcServerPortHttps(int grpcServerPortHttps) {
        this.grpcServerPortHttps = grpcServerPortHttps;
    }

    public String getKeyStoreFileName() {
        return keyStoreFileName;
    }

    public void setKeyStoreFileName(String keyStoreFileName) {
        this.keyStoreFileName = keyStoreFileName;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public boolean isHttpsServerEnabled() {
        return httpsServerEnabled;
    }

    public void setHttpsServerEnabled(boolean httpsServerEnabled) {
        this.httpsServerEnabled = httpsServerEnabled;
    }

    public int getHttpsServerPort() {
        return httpsServerPort;
    }

    public void setHttpsServerPort(int httpsServerPort) {
        this.httpsServerPort = httpsServerPort;
    }

    public boolean isInsecureMode() {
        return insecureMode;
    }

    public void setInsecureMode(boolean insecureMode) {
        this.insecureMode = insecureMode;
    }

    @Override
    public String toString() {
        return "ThingverseBackendProperties{" +
                "httpsServerEnabled=" + httpsServerEnabled +
                ", insecureMode=" + insecureMode +
                ", httpsServerPort=" + httpsServerPort +
                ", keyStoreFileName='" + keyStoreFileName + '\'' +
                ", keyStorePassword='" + keyStorePassword + '\'' +
                ", backendAkkaConfigFile='" + backendAkkaConfigFile + '\'' +
                ", roles=" + Arrays.toString(roles) +
                ", cassandraContactPoints=" + Arrays.toString(cassandraContactPoints) +
                ", cassandraLocalDatacenter='" + cassandraLocalDatacenter + '\'' +
                ", dumpActorSystemConfig=" + dumpActorSystemConfig +
                ", printInitialActorSystemTree=" + printInitialActorSystemTree +
                ", grpcServerHost='" + grpcServerHost + '\'' +
                ", grpcServerPort=" + grpcServerPort +
                ", grpcServerPortHttps=" + grpcServerPortHttps +
                ", akkaRemotePort=" + akkaRemotePort +
                ", akkaManagementHttpPort=" + akkaManagementHttpPort +
                ", actorTimeoutDuration='" + actorTimeoutDuration + '\'' +
                ", eventsBeforeSnapshotting=" + eventsBeforeSnapshotting +
                ", maxSnapshotsToKeep=" + maxSnapshotsToKeep +
                ", operationTimeoutDuration='" + operationTimeoutDuration + '\'' +
                ", backendOperationMode=" + backendOperationMode +
                ", clusterBootstrapServiceDiscoveryMethod=" + clusterBootstrapServiceDiscoveryMethod +
                ", akkaManagementHttpPortMin=" + akkaManagementHttpPortMin +
                ", akkaManagementHttpPortMax=" + akkaManagementHttpPortMax +
                ", actorSystemName='" + actorSystemName + '\'' +
                '}';
    }
}
