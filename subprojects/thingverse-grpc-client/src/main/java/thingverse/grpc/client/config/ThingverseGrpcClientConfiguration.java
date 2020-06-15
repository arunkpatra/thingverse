package thingverse.grpc.client.config;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.discovery.Discovery;
import akka.discovery.ServiceDiscovery;
import akka.grpc.GrpcClientSettings;
import akka.stream.Materializer;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClient;
import com.thingverse.backend.client.v1.EnhancedThingverseGrpcServiceClientImpl;
import com.thingverse.backend.v1.ThingverseGrpcServiceClient;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import grpc.health.v1.HealthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import thingverse.kubernetes.annotation.EnableKubernetesLookup;
import thingverse.kubernetes.config.KubernetesLookupProperties;
import thingverse.tracing.annotation.EnableThingverseTracing;
import thingverse.tracing.config.ThingverseTracer;


import static com.thingverse.common.utils.ConsoleColors.thanos;

@Configuration
@EnableConfigurationProperties(ThingverseGrpcClientProperties.class)
@EnableThingverseTracing
@EnableKubernetesLookup
@ConditionalOnProperty(prefix = "thingverse.grpc.client", name = {"enabled"}, matchIfMissing = true)
public class ThingverseGrpcClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingverseGrpcClientConfiguration.class);

    private final ThingverseGrpcClientProperties properties;
    private final KubernetesLookupProperties kubernetesLookupProperties;
    private final ThingverseTracer tracer;

    public ThingverseGrpcClientConfiguration(ThingverseGrpcClientProperties properties,
                                             KubernetesLookupProperties kubernetesLookupProperties,
                                             ThingverseTracer tracer) {
        this.properties = properties;
        this.kubernetesLookupProperties = kubernetesLookupProperties;
        this.tracer = tracer;
    }


    // We force creation of an exclusive actor system for the client to ensure that it uses the right discovery method
    @Bean(name = "thingverseApiGrpcClientActorSystem")
    public ActorSystem<Void> clientActorSystem() {
        LOGGER.info("Creating the client ActorSystem.");
        return ActorSystem.create(Behaviors.empty(), properties.getClientName().concat("-actor-system"),
                        getClientActorSystemConfig(properties));
    }

    private Config getClientActorSystemConfig(ThingverseGrpcClientProperties properties) {
        return customConfigOverrides(properties).withFallback(defaultConfig());
    }

    /**
     * We can process any custom files here.
     *
     * @return The default config
     */
    private Config defaultConfig() {
        return ConfigFactory.load();
    }

    private Config customConfigOverrides(ThingverseGrpcClientProperties properties) {
        // Default is Consul discovery, we switch the discovery method to DNS if it is required.
        String discoveryMethodDetails = "";
        switch (properties.getDiscoveryMechanism()) {
            case DNS:
                discoveryMethodDetails = "akka.discovery.method = " + "akka-dns" + "\n" +
                        "akka.io.dns.resolver = " + "async-dns" + "\n";
                break;
            case KUBERNETES_SERVICE:
                discoveryMethodDetails = "akka.discovery.method = " + "kubernetes-service" + "\n" +
                "akka.discovery.kubernetes-service.service-namespace = " +
                        kubernetesLookupProperties.getServiceLookupNamespace() + "\n";
                break;
            default:
                break;
        }

        return ConfigFactory.parseString(discoveryMethodDetails);
    }

    private GrpcClientSettings getGrpcClientSettings(ActorSystem<Void> actorSystem) {
        GrpcClientSettings settings;

        switch (properties.getDiscoveryMechanism()) {
            case KUBERNETES_SERVICE:
                ServiceDiscovery kubernetesApiServiceDiscovery =
                        Discovery.get(actorSystem.classicSystem()).loadServiceDiscovery("kubernetes-service");
                settings = GrpcClientSettings
                        .usingServiceDiscovery(properties.getServiceName()
                                        .concat(".")
                                        .concat(kubernetesLookupProperties.getServiceLookupNamespace())
                                        .concat(".svc.cluster.local:8080"),
                                kubernetesApiServiceDiscovery,
                                actorSystem.classicSystem())
                        .withServiceProtocol("tcp")
                        .withServicePortName("http")
                        //.withGrpcLoadBalancingType("round_robin")
                        .withTls(properties.isUseTls());
                break;
            case CONSUL:
                settings = GrpcClientSettings
                        .usingServiceDiscovery(properties.getServiceName(), actorSystem.classicSystem())
                        .withTls(properties.isUseTls());
                break;
            // for Akka DNS to work you just supply these to Akka. You can use System properties via -D style
            // properties or by default config to Akka.
            // akka {
            //  discovery.method = akka-dns
            //  io.dns.resolver = async-dns
            //}
            case DNS:
                settings = GrpcClientSettings
                        .usingServiceDiscovery(
                                properties.getServiceName().concat(".thingverse.svc.cluster.local"),
                                actorSystem.classicSystem())
                        .withServicePortName("http")
                        .withServiceProtocol("tcp")
                        .withConnectionAttempts(3)
                        .withGrpcLoadBalancingType("round_robin")
                        .withTls(properties.isUseTls());
                break;
            case STATIC:
                settings = GrpcClientSettings.fromConfig(properties.getClientName(), actorSystem.classicSystem())
                        .withConnectionAttempts(5000);
                break;
            default:
                throw new IllegalArgumentException("Invalid service lookup mechanism requested.");
        }

        return settings;
    }
    @Bean
    @ConditionalOnMissingBean({EnhancedThingverseGrpcServiceClient.class})
    public EnhancedThingverseGrpcServiceClient thingverseGrpcServiceClient(ActorSystem<Void> actorSystem) {
        Materializer materializer = Materializer.matFromSystem(actorSystem.classicSystem());
        GrpcClientSettings settings = getGrpcClientSettings(actorSystem);
        LOGGER.info("gRPC client {} will use {} for looking up gRPC service {}. May the force be with you...",
                thanos(properties.getClientName()),
                thanos(settings.serviceDiscovery().getClass().getSimpleName()),
                thanos(settings.serviceName()));
        return new EnhancedThingverseGrpcServiceClientImpl(ThingverseGrpcServiceClient
                .create(settings, materializer, actorSystem.classicSystem().dispatcher()), tracer);
    }

    @Bean
    @ConditionalOnMissingBean({HealthClient.class})
    public HealthClient thingverseBackendHealthCheckClient(ActorSystem<Void> actorSystem) {
        Materializer materializer = Materializer.matFromSystem(actorSystem.classicSystem());
        GrpcClientSettings settings =getGrpcClientSettings(actorSystem);
        return HealthClient.create(settings, materializer, actorSystem.classicSystem().dispatcher());
    }
}
