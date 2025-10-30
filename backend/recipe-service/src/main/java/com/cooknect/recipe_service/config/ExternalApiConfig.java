package com.cooknect.recipe_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ExternalApiConfig {
    @Value("${spoonacular.api.base-url}")
    private String spoonacularBaseUrl;

    @Bean
    public WebClient spoonacularWebClient() {
        return WebClient.builder()
                .baseUrl(spoonacularBaseUrl)
                .build();
    }
}
