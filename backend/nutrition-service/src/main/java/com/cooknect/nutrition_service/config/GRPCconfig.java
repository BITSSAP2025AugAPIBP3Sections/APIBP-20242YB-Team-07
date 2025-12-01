package com.cooknect.nutrition_service.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;

import com.recipe.RecipeServiceGrpc;

import java.util.concurrent.TimeUnit;

/**
 * gRPC Configuration for Nutrition Service
 * Configures gRPC clients for communicating with Recipe Service
 */
@Configuration
public class GRPCconfig {

    private static final Logger logger = LoggerFactory.getLogger(GRPCconfig.class);

    @Value("${grpc.client.recipe-service.address:localhost:9090}")
    private String recipeServiceAddress;

    @Value("${grpc.client.recipe-service.negotiationType:PLAINTEXT}")
    private String negotiationType;

    @Value("${grpc.client.recipe-service.max-inbound-message-size:4194304}")
    private int maxInboundMessageSize;

    @Value("${grpc.client.recipe-service.keepalive-time:30}")
    private long keepaliveTime;

    @Value("${grpc.client.recipe-service.keepalive-timeout:10}")
    private long keepaliveTimeout;

    /**
     * Creates a managed channel for the Recipe Service gRPC client
     * Configured with connection pooling, keepalive, and message size limits
     */
    @Bean(name = "recipeServiceChannel")
    public ManagedChannel recipeServiceChannel() {
        logger.info("Initializing gRPC channel for Recipe Service at: {}", recipeServiceAddress);

        String[] addressParts = recipeServiceAddress.split(":");
        String host = addressParts[0];
        int port = addressParts.length > 1 ? Integer.parseInt(addressParts[1]) : 9090;

        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress(host, port);

        // Configure connection type (PLAINTEXT for development, TLS for production)
        if ("PLAINTEXT".equalsIgnoreCase(negotiationType)) {
            channelBuilder.usePlaintext();
        }

        return channelBuilder
                .maxInboundMessageSize(maxInboundMessageSize)
                .keepAliveTime(keepaliveTime, TimeUnit.SECONDS)
                .keepAliveTimeout(keepaliveTimeout, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();
    }

    /**
     * Creates a blocking stub for Recipe Service
     * Used for synchronous gRPC calls to fetch recipe data
     * NOTE: Deadline should be set per-call, not on the stub
     */
    @Bean
    public RecipeServiceGrpc.RecipeServiceBlockingStub recipeServiceStub(
            ManagedChannel recipeServiceChannel) {
        logger.info("Creating RecipeService gRPC stub");
        return RecipeServiceGrpc.newBlockingStub(recipeServiceChannel);
        // Deadline is set per-call in RecipeGrpcClient to avoid "deadline already exceeded" errors
    }

    /**
     * Global client interceptor for logging and monitoring
     * Applied to all outgoing gRPC calls
     */
    @GrpcGlobalClientInterceptor
    public ClientInterceptor loggingInterceptor() {
        return new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    MethodDescriptor<ReqT, RespT> method,
                    CallOptions callOptions,
                    Channel next) {

                logger.debug("gRPC call initiated: {}", method.getFullMethodName());

                return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                        next.newCall(method, callOptions)) {

                    @Override
                    public void start(Listener<RespT> responseListener, Metadata headers) {
                        // Add custom headers if needed
                        // headers.put(Metadata.Key.of("custom-header", Metadata.ASCII_STRING_MARSHALLER), "value");

                        super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            public void onClose(io.grpc.Status status, Metadata trailers) {
                                if (status.isOk()) {
                                    logger.debug("gRPC call completed successfully: {}", method.getFullMethodName());
                                } else {
                                    logger.error("gRPC call failed: {} - {}",
                                            method.getFullMethodName(), status.getDescription());
                                }
                                super.onClose(status, trailers);
                            }
                        }, headers);
                    }
                };
            }
        };
    }

    /**
     * Gracefully shutdown gRPC channels when application stops
     */
    @Bean
    public GrpcChannelShutdownHook grpcChannelShutdownHook(ManagedChannel recipeServiceChannel) {
        return new GrpcChannelShutdownHook(recipeServiceChannel);
    }

    /**
     * Hook to ensure proper cleanup of gRPC resources
     */
    private static class GrpcChannelShutdownHook {
        private final ManagedChannel channel;

        public GrpcChannelShutdownHook(ManagedChannel channel) {
            this.channel = channel;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logger.info("Shutting down gRPC channel...");
                    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                    logger.info("gRPC channel shutdown complete");
                } catch (InterruptedException e) {
                    logger.error("Error during gRPC channel shutdown", e);
                    Thread.currentThread().interrupt();
                }
            }));
        }
    }
}
