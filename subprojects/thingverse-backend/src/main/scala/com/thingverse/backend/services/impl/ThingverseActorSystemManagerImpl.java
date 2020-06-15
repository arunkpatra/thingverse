package com.thingverse.backend.services.impl;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.cluster.typed.Cluster;
import akka.dispatch.OnComplete;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSessionRegistry;
import com.thingverse.api.storage.ThingverseAkkaStorageBackend;
import com.thingverse.backend.behaviors.ThingverseGuardianBehavior;
import com.thingverse.backend.config.BackendOperationMode;
import com.thingverse.backend.config.ClusterBootstrapServiceDiscoveryMethod;
import com.thingverse.backend.config.ThingverseBackendProperties;
import com.thingverse.backend.models.ActorSystemInfo;
import com.thingverse.backend.models.ActorSystemInfoFormatted;
import com.thingverse.backend.models.ActorSystemStatus;
import com.thingverse.backend.services.ThingverseActorSystemManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.SocketUtils;
import org.springframework.util.StringUtils;
import thingverse.discovery.consul.config.ConsulRegistrationProperties;
import thingverse.kubernetes.config.KubernetesLookupProperties;

import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.time.Duration;

import java.util.*;

import static com.thingverse.common.utils.ConsoleColors.thanos;
import static java.time.temporal.ChronoUnit.SECONDS;

@ManagedResource(objectName="thingverse:name=ThingverseActorSystemManager", description="Thingverse Akka ActorSystem Manager Managed Bean")
public class ThingverseActorSystemManagerImpl implements ThingverseActorSystemManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseActorSystemManagerImpl.class);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz");
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final InetUtils inetUtils;
    private final ThingverseBackendProperties properties;
    private final KubernetesLookupProperties kubernetesLookupProperties;
    private final ThingverseAkkaStorageBackend storageBackend;
    private final Config actorSystemConfig;
    private final ActorSystemInfo actorSystemInfo;

    public ThingverseActorSystemManagerImpl(ThingverseBackendProperties properties,
                                            KubernetesLookupProperties kubernetesLookupProperties,
                                            ConsulRegistrationProperties consulRegistrationProperties,
                                            InetUtils inetUtils, ThingverseAkkaStorageBackend storageBackend) {
        LOGGER.info("Initializing {} with properties: {}", this.getClass().getSimpleName(), properties.toString());
        this.inetUtils = inetUtils;
        this.properties = properties;
        this.kubernetesLookupProperties = kubernetesLookupProperties;
        this.storageBackend = storageBackend;
        Config configOverrides = customConfigOverrides(properties, consulRegistrationProperties, inetUtils);
        this.actorSystemConfig = configOverrides.withFallback(defaultConfig(properties));
        this.actorSystemInfo = new ActorSystemInfo(Optional.empty(), Optional.empty(), Optional.empty(),
                ActorSystemStatus.UNKNOWN);

        LOGGER.info("Akka is using overrides: {}",
                configOverrides.resolve().root().render(ConfigRenderOptions.concise()));
        if (properties.isDumpActorSystemConfig()) {
            LOGGER.info("Akka will start with this effective configuration: {}", this.actorSystemConfig.toString());
        }
    }

    private Config defaultConfig(ThingverseBackendProperties properties) {
        Config defaultConfig = ConfigFactory.load();
        //String configFileName;
        switch (properties.getBackendOperationMode()) {
            case CLUSTER:
                String configFileNameCluster = StringUtils.isEmpty(properties.getBackendAkkaConfigFile()) ?
                        ThingverseBackendProperties.DEFAULT_BACKEND_AKKA_CLUSTER_CONFIG_FILE : properties.getBackendAkkaConfigFile();
                return ConfigFactory.parseResources(configFileNameCluster).withFallback(defaultConfig);
//                return ConfigFactory.load().withFallback(ConfigFactory.parseFile(new File(configFileNameCluster)));
            //break;
            case STANDALONE:
                String configFileNameStandalone = StringUtils.isEmpty(properties.getBackendAkkaConfigFile()) ?
                        ThingverseBackendProperties.DEFAULT_BACKEND_AKKA_STANDALONE_CONFIG_FILE : properties.getBackendAkkaConfigFile();
                return ConfigFactory.parseResources(configFileNameStandalone).withFallback(defaultConfig);
            //break;
            default:
                throw new IllegalStateException("Invalid Operation mode. Only `cluster` and `standalone` are allowed.");
        }
        //LOGGER.info("Reading backend Akka configs from {}", configFileName);
//        return ConfigFactory.parseFile(new File(properties.getBackendAkkaConfigFile()));
        //return ConfigFactory.parseFile(new File(properties.getBackendAkkaConfigFile()));
    }

    @Override
    @ManagedOperation(description="Create a new Akka ActorSystem")
    public ActorSystemInfo createActorSystem() {
        if (ActorSystemStatus.STARTING.equals(this.actorSystemInfo.getStatus()) ||
                ActorSystemStatus.STARTED.equals(this.actorSystemInfo.getStatus())) {
            LOGGER.warn("An Akka ActorSystem already exists or is starting, can not create a new one right now. " +
                    "You must terminate the other one first.");
            return this.actorSystemInfo;
        }
        LOGGER.info(thanos("Initializing Akka actor system : {}"), properties.getActorSystemName());
        // Set initial status
        this.actorSystemInfo.setStatus(ActorSystemStatus.STARTING);

        ActorSystem<Void> actorSystem =
                ActorSystem.create(ThingverseGuardianBehavior.create(this.storageBackend.getEventProcessorStream()),
                        this.properties.getActorSystemName(), this.actorSystemConfig);
        // Register a callback
        actorSystem.whenTerminated().onComplete(new OnComplete<Done>() {
            @Override
            public void onComplete(Throwable failure, Done success) {
                LOGGER.info(">>>>> Actor system has terminated.");
                actorSystemInfo.setStatus(ActorSystemStatus.TERMINATED);
                actorSystemInfo.setActorSystem(Optional.empty());
                actorSystemInfo.setAddress(Optional.empty());
                actorSystemInfo.setRoles(Optional.empty());
            }
        }, actorSystem.executionContext());

        LOGGER.info("@@@@@@@@@@@ Will call method to create tables");
        createTablesIfNeeded(actorSystem, this.storageBackend);

        // Set status values
        actorSystemInfo.setStatus(ActorSystemStatus.STARTED);
        actorSystemInfo.setActorSystem(Optional.of(actorSystem));
        actorSystemInfo.setAddress(Optional.of(actorSystem.address().toString()));
        actorSystemInfo.setRoles(
                Optional.of(scala.collection.JavaConverters.setAsJavaSet(Cluster.get(actorSystem).selfMember().roles())));

        if (properties.isPrintInitialActorSystemTree()) {
            LOGGER.info("Printing initial Actor system tree. \n{}", actorSystem.printTree());
        }
        LOGGER.info(thanos("Initialized Akka actor system: {}"), actorSystemInfo.toString());
        return actorSystemInfo;
    }

    @Override
    public ActorSystemInfo getActorSystemInfo() {
        return this.actorSystemInfo;
    }

    @ManagedAttribute(description = "Information about the Akka ActorSystem managed by this MBean")
    public ActorSystemInfoFormatted getActorSystemInfoFormatted() {
        return formatActorSystemInfo(this.actorSystemInfo);
    }

    private ActorSystemInfoFormatted formatActorSystemInfo(ActorSystemInfo a) {
        a.getActorSystem().get().startTime();
        String startTime = "UNAVAILABLE";
        String upTime = "UNAVAILABLE";
        if (a.getActorSystem().isPresent()) {
            startTime = sdf.format(new Date(a.getActorSystem().get().startTime()));
            upTime = Duration.of(a.getActorSystem().get().uptime(), SECONDS).toString();
        }
        return new ActorSystemInfoFormatted(
                        (a.getActorSystem().isPresent() ? a.getActorSystem().get().name() : "UNAVAILABLE"),
                        a.getRoles().isPresent() ? a.getRoles().get().toString() : "UNAVAILABLE",
                        a.getAddress().isPresent() ? a.getAddress().get() : "UNAVAILABLE",
                        a.getStatus(), upTime, startTime);
    }
    @Override
    @ManagedOperation(description="Terminate the Akka ActorSystem managed by this MBean")
    public ActorSystemInfoFormatted terminateActorSystem() {
        this.actorSystemInfo.setStatus(ActorSystemStatus.TERMINATING);
        this.actorSystemInfo.getActorSystem().ifPresent(ActorSystem::terminate);
        return formatActorSystemInfo(this.actorSystemInfo);
    }

    @Override
    @ManagedAttribute(description = "Hierarchy of actors contained in the Akka ActorSystem managed by this MBean")
    public String getActorSystemTree() {
        if (actorSystemInfo.getActorSystem().isPresent()) {
            return actorSystemInfo.getActorSystem().get().printTree();
        } else {
            return "ActorSystem not available.";
        }
    }

    @Override
    @ManagedAttribute(description = "Core settings used by the Akka ActorSystem managed by this MBean")
    public String getActorSystemSettings() {
        if (actorSystemInfo.getActorSystem().isPresent()) {
            return actorSystemInfo.getActorSystem().get().settings().toString();
        } else {
            return "ActorSystem not available.";
        }
    }

    private void createTablesIfNeeded(ActorSystem<Void> system, ThingverseAkkaStorageBackend storageBackend) {
        LOGGER.info("@@@@@@@@@@@ In method to create tables");
        if (Cluster.get(system).selfMember().hasRole("read-model")) {
            Map<String, Object> backendContext = new HashMap<>();
            CassandraSession session =
                    CassandraSessionRegistry.get(system).sessionFor("alpakka.cassandra");
            backendContext.put("cassandra-session", session);
            LOGGER.info("@@@@@@@@@@@ Calling init method on backend");
            storageBackend.init(backendContext);
        }
    }

    private Config customConfigOverrides(ThingverseBackendProperties props,
                                         ConsulRegistrationProperties consulRegistrationProperties,
                                         InetUtils inetUtils) {
        int akkaMgmtHttpPort = props.getAkkaManagementHttpPort();
        if (akkaMgmtHttpPort == 0) {
            akkaMgmtHttpPort = SocketUtils.findAvailableTcpPort(props.getAkkaManagementHttpPortMin(),
                    props.getAkkaManagementHttpPortMax());
        }
        String akkaMgmtBindHostName = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
        String akkaRemoteHostname;

        if (BackendOperationMode.STANDALONE.equals(props.getBackendOperationMode())) {
            LOGGER.info(">>>> This is standalone");
            akkaRemoteHostname = "akka.remote.artery.canonical.hostname = 127.0.0.1 \n";
        } else {
            LOGGER.info(">>>> This is clustered");
            akkaRemoteHostname = "akka.remote.artery.canonical.hostname = " + akkaMgmtBindHostName + " \n";
        }
//         akkaRemoteHostname = BackendOperationMode.STANDALONE.name().equalsIgnoreCase(props.getBackendOperationMode().name().toLowerCase()) ?
//                "akka.remote.artery.canonical.hostname = 127.0.0.1 \n" : "akka.remote.artery.canonical.hostname = " + akkaMgmtBindHostName + " \n";

        String akkaDiscoveryMethod = "", consulHostInfo = "", consulPortInfo = "";
        String kubernetesPodLookupNamespace = "";

        // akka-consul discovery in use
        if (ClusterBootstrapServiceDiscoveryMethod.CONSUL.equals(props.getClusterBootstrapServiceDiscoveryMethod())) {
            akkaDiscoveryMethod = consulRegistrationProperties.isEnabled() ?
                    "akka.discovery.method = " + "akka-consul" + "\n" : "";
            consulHostInfo = consulRegistrationProperties.isEnabled() ?
                    "akka.discovery.akka-consul.consul-host = " + consulRegistrationProperties.getHost() + "\n" : "";
            consulPortInfo = consulRegistrationProperties.isEnabled() ?
                    "akka.discovery.akka-consul.consul-port = " + consulRegistrationProperties.getPort() + "\n" : "";
        }
        // kubernetes-api discovery in use
        if (ClusterBootstrapServiceDiscoveryMethod.KUBERNETES.equals(props.getClusterBootstrapServiceDiscoveryMethod())) {
            akkaDiscoveryMethod = "akka.discovery.method = " + "kubernetes-api" + "\n";
            kubernetesPodLookupNamespace = "akka.discovery.kubernetes-api.pod-namespace = " +
                    kubernetesLookupProperties.getPodLookupNamespace() + "\n";
        }

        String cassandraContactPoints = props.getCassandraContactPoints().length > 0 ?
                "datastax-java-driver.basic.contact-points = " + Arrays.toString(props.getCassandraContactPoints()) + "\n" : "";

        String cassandraLocalDatacenter =
                "datastax-java-driver.basic.load-balancing-policy.local-datacenter = " + props.getCassandraLocalDatacenter() + "\n";

        return ConfigFactory.parseString(
                akkaRemoteHostname +  cassandraContactPoints + cassandraLocalDatacenter + akkaDiscoveryMethod + consulHostInfo +
                        consulPortInfo + kubernetesPodLookupNamespace +
                        "akka.cluster.roles = " + Arrays.toString(props.getRoles()) + "\n" +
                        "akka.management.http.port = " + akkaMgmtHttpPort + "\n" +
                        "akka.management.http.hostname = " + akkaMgmtBindHostName + "\n" +
                        "akka.management.http.bind-hostname = " + akkaMgmtBindHostName + "\n" +
                        "akka.remote.artery.canonical.port = " + props.getAkkaRemotePort() + "\n" +
                        "akka.http.server.preview.enable-http2 = on" + "\n" +
                        "thingverse.async-operation-timeout-duration = " + props.getOperationTimeoutDuration() + "\n");
    }

    @PreDestroy
    public void shutdownActorSystem() {
        //LOGGER.info("Terminating actor system named {}", actorSystem.name());
        this.actorSystemInfo.getActorSystem().ifPresent(ActorSystem::terminate);
    }
}
