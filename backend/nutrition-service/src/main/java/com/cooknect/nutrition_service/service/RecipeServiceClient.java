package com.cooknect.nutrition_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Component
public class RecipeServiceClient {

    private final WebClient webClient;

    public RecipeServiceClient(@Value("${recipe.service.base-url:http://localhost:8081}") String baseUrl,
                               WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Optional<RecipeDto> getRecipeById(Long id) {
        try {
            RecipeDto dto = webClient.get()
                    .uri("/api/v1/recipes/{id}", id)
                    .retrieve()
                    .bodyToMono(RecipeDto.class)
                    .block();
            return Optional.ofNullable(dto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    
    public static class RecipeDto {
        private Long id;
        private String title;
        private List<IngredientDto> ingredients;
        private String username;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<IngredientDto> getIngredients() { return ingredients; }
        public void setIngredients(List<IngredientDto> ingredients) { this.ingredients = ingredients; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class IngredientDto {
        private String name;
        private String quantity;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
    }
}