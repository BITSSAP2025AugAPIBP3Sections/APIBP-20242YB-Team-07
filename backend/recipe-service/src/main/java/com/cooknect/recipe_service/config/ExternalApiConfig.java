package com.cooknect.recipe_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ExternalApiConfig {
    @Bean
    public WebClient spoonacularWebClient() {
        return WebClient.builder().build();
    }
}
