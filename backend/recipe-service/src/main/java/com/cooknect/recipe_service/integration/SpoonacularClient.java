package com.cooknect.recipe_service.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class SpoonacularClient {

    private final WebClient webClient;

    @Value("${spoonacular.api.key}")
    private String apiKey;

    public SpoonacularClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.spoonacular.com").build();
    }

    // Example: Search recipes by query
    public String searchRecipes(String query) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/recipes/complexSearch")
                            .queryParam("query", query)
                            .queryParam("apiKey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // convert async Mono -> sync String
        } catch (WebClientResponseException ex) {
            System.err.println("API Error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
            return "{\"error\":\"Failed to fetch recipes\"}";
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return "{\"error\":\"Service unavailable\"}";
        }
    }
}
