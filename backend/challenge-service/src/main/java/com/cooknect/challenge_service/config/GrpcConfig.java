package com.cooknect.challenge_service.config;

import com.recipe.RecipeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class GrpcConfig {

    @Value("${grpc.client.recipe-service.address}")
    private String recipeServiceAddress;

    @Bean
    public ManagedChannel recipeServiceChannel() {
        String[] addressParts = recipeServiceAddress.replace("static://", "").split(":");
        String host = addressParts[0];
        int port = Integer.parseInt(addressParts[1]);

        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public RecipeServiceGrpc.RecipeServiceBlockingStub recipeServiceStub(ManagedChannel recipeServiceChannel) {
        return RecipeServiceGrpc.newBlockingStub(recipeServiceChannel);
    }
}
