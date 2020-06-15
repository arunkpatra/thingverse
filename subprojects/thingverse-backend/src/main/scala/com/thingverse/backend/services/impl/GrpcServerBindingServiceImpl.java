package com.thingverse.backend.services.impl;

import akka.actor.typed.ActorSystem;
import akka.grpc.javadsl.ServiceHandler;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.HttpsConnectionContext;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Function;
import akka.stream.Materializer;
import com.thingverse.backend.config.ThingverseBackendProperties;
import com.thingverse.backend.services.GrpcServerBindingService;
import com.thingverse.security.utils.TlsUtils;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static akka.http.javadsl.ConnectHttp.toHostHttps;

public class GrpcServerBindingServiceImpl implements GrpcServerBindingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcServerBindingServiceImpl.class);
    private final ThingverseBackendProperties properties;
    public GrpcServerBindingServiceImpl(ThingverseBackendProperties properties) {
        this.properties = properties;
    }

    @Override
    public GrpcServerBindingStatuses bindAndServe(ActorSystem<Void> sys,
                                                Function<HttpRequest, CompletionStage<HttpResponse>>[] handlers) {
        GrpcServerBindingStatuses statuses = new GrpcServerBindingStatuses();
        // Serve on HTTP also
        GrpcServerBindingStatuses s1 = bindAndServeInternalHttp(sys, handlers);
        statuses.getServerBindingStatusList().addAll(s1.getServerBindingStatusList());

        // Now serve HTTPS
        if (properties.httpsServerEnabled) {
            SSLContext sslContext = TlsUtils.getSslContext(properties.getKeyStoreFileName(),properties.getKeyStorePassword(), properties.isInsecureMode());
            HttpsConnectionContext https = ConnectionContext.https(sslContext);
            Materializer mat = Materializer.matFromSystem(sys.classicSystem());
            Function<HttpRequest, CompletionStage<HttpResponse>> serviceHandlers = ServiceHandler.concatOrNotFound(handlers);
            try {
                GrpcServerBindingStatus status =  Http.get(sys.classicSystem())
                        .bindAndHandleAsync(serviceHandlers, toHostHttps(this.properties.getGrpcServerHost(),
                                this.properties.getGrpcServerPortHttps()).withCustomHttpsContext(https), mat)
                        .handleAsync((b, t) -> new GrpcServerBindingStatus(b.localAddress().toString(),
                                b.localAddress().getPort(), null != t, "https"))
                        .whenComplete((b, t) -> LOGGER.info("gRPC HTTP Server binding result: {}", b))
                        .toCompletableFuture().get(10, TimeUnit.SECONDS);
                statuses.getServerBindingStatusList().add(status);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new IllegalStateException("gRPC HTTPS Server failure. Can not proceed.", e);
            }
        }
        return statuses;
    }

    private GrpcServerBindingStatuses bindAndServeInternalHttp(ActorSystem<Void> sys,
                                                  Function<HttpRequest, CompletionStage<HttpResponse>>[] handlers) {
        GrpcServerBindingStatuses statuses = new GrpcServerBindingStatuses();
        Materializer mat = Materializer.matFromSystem(sys.classicSystem());
        Function<HttpRequest, CompletionStage<HttpResponse>> serviceHandlers = ServiceHandler.concatOrNotFound(handlers);
        try {
            GrpcServerBindingStatus status =  Http.get(sys.classicSystem())
                    .bindAndHandleAsync(serviceHandlers, ConnectHttp.toHost(this.properties.getGrpcServerHost(),
                            this.properties.getGrpcServerPort()), mat)
                    .handleAsync((b, t) -> new GrpcServerBindingStatus(b.localAddress().toString(),
                            b.localAddress().getPort(), null != t, "http"))
                    .whenComplete((b, t) -> LOGGER.info("gRPC HTTP Server binding result: {}", b))
                    .toCompletableFuture().get(10, TimeUnit.SECONDS);
            statuses.getServerBindingStatusList().add(status);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException("gRPC HTTP Server failure. Can not proceed.", e);
        }
        return statuses;
    }
}
