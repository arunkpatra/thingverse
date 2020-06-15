package grpc.health.v1;

import akka.NotUsed;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GrpcHealthServiceImpl implements Health {
    @Override
    public CompletionStage<HealthCheckResponse> check(HealthCheckRequest in) {
        return CompletableFuture.supplyAsync(
                () -> HealthCheckResponse.newBuilder().setStatus(HealthCheckResponse.ServingStatus.SERVING).build());
    }

    @Override
    public Source<HealthCheckResponse, NotUsed> watch(HealthCheckRequest in) {
        throw new UnsupportedOperationException();
    }
}
