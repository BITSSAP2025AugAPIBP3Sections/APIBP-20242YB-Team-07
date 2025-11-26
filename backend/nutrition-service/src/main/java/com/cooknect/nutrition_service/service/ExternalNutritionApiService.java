package com.cooknect.nutrition_service.service;

import com.cooknect.nutrition_service.model.FoodItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@SuppressWarnings("unused")
@Service
public class ExternalNutritionApiService {

    private final WebClient webClient;

    @Value("${nutrition.api.ninjas.base-url}")
    private String apiBaseUrl;
    @Value("${nutrition.api.ninjas.app-key}")
    private String appKey;

    public ExternalNutritionApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Optional<FoodItem> fetchNutritionInfo(String query) {
        List<Map<String, Object>> responseList = webClient.get()
                .uri(apiBaseUrl, uriBuilder -> uriBuilder
                        .queryParam("query", query)
                        .build())
                .header("X-Api-Key", appKey) 
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block(); 

        if (responseList != null && !responseList.isEmpty()) {
            Map<String, Object> data = responseList.get(0); 

            FoodItem foodItem = new FoodItem();
            foodItem.setFoodItem(safeToString(data.get("name")));
            foodItem.setServingSize(safeToString(data.get("serving_size_g")));
            foodItem.setTotalFat(safeToDouble(data.get("fat_total_g")));
            foodItem.setSodium(safeToDouble(data.get("sodium_mg")));
            foodItem.setPotassium(safeToDouble(data.get("potassium_mg")));
            foodItem.setCholestrol(safeToDouble(data.get("cholesterol_mg")));
            foodItem.setCarbohydrates(safeToDouble(data.get("carbohydrates_total_g")));
            foodItem.setFiber(safeToDouble(data.get("fiber_g")));
            foodItem.setSugar(safeToDouble(data.get("sugar_g")));
            return Optional.of(foodItem);
        }

        return Optional.empty();
    }

    private Double safeToDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o instanceof String) {
            try { return Double.parseDouble(((String) o).trim()); }
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private String safeToString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private double getNumericValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            String s = ((String) value).trim();
            // try direct parse first (covers "77.7" etc.)
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {}
            // fallback: extract numeric characters (handles strings like "100 g" or "Only available...")
            String numeric = s.replaceAll("[^0-9.\\-]+", "");
            if (numeric.isEmpty()) return 0.0;
            try {
                return Double.parseDouble(numeric);
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }
        return 0.0;
    }
}