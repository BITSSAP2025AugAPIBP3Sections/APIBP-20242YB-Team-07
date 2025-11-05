package com.cooknect.nutrition_service.controller;

import com.cooknect.nutrition_service.dto.DailyIntakeSummary;
import com.cooknect.nutrition_service.dto.NutritionRequest;
import com.cooknect.nutrition_service.dto.NutritionResponse;
import com.cooknect.nutrition_service.model.FoodItem;
import com.cooknect.nutrition_service.model.MealType;
import com.cooknect.nutrition_service.model.NutritionLog;
import com.cooknect.nutrition_service.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NutritionGraphqlController {

    private final NutritionService nutritionService;

    // --- QUERIES ---

    @QueryMapping
    public List<FoodItem> allFoodItems() {
        return nutritionService.getAllFoodItems();
    }

    @QueryMapping
    public List<NutritionLog> allNutritionLogs() {
        return nutritionService.getAllNutritionLogs();
    }

    @QueryMapping
    public List<NutritionLog> logsByUserName(@Argument String userName) {
        return nutritionService.getNutritionLogsByUserName(userName);
    }

    @QueryMapping
    public List<NutritionLog> logsByMealType(@Argument String userName, @Argument MealType mealType) {
        return nutritionService.getNutritionLogsByMealType(userName, mealType);
    }

    @QueryMapping
    public DailyIntakeSummary todayIntakeSummary(@Argument String userName) {
        return nutritionService.getTodayIntakeSummary(userName);
    }

    // --- MUTATIONS ---

    @MutationMapping
    public FoodItem addFoodItem(@Argument("foodItem") FoodItem foodItem) {
        return nutritionService.addFoodItem(foodItem);
    }

    private NutritionRequest buildRequestFromMap(Map<String, Object> requestMap) {
        NutritionRequest request = new NutritionRequest();
        request.setUserName((String) requestMap.get("userName"));
        request.setRecipeId(requestMap.get("recipeId") != null ? Long.parseLong(requestMap.get("recipeId").toString()) : null);
        request.setRecipeName((String) requestMap.get("recipeName"));
        
        if (requestMap.get("mealType") != null) {
            request.setMealType(MealType.valueOf(requestMap.get("mealType").toString().toUpperCase()));
        }

        Object ingredientsObj = requestMap.get("ingredients");
        if (ingredientsObj instanceof List) {
            List<Map<String, String>> ingredientsList = new ArrayList<>();
            List<?> rawList = (List<?>) ingredientsObj;
            for (Object item : rawList) {
                if (item instanceof Map) {
                    // This cast is now much safer
                    @SuppressWarnings("unchecked")
                    Map<String, String> ingredientMap = (Map<String, String>) item;
                    ingredientsList.add(ingredientMap);
                }
            }
            request.setIngredients(ingredientsList);
        } else {
            request.setIngredients(Collections.emptyList());
        }
        
        return request;
    }
    
    @MutationMapping
    public NutritionResponse analyzeIngredients(@Argument("request") Map<String, Object> requestMap) {
        NutritionRequest request = buildRequestFromMap(requestMap);
        return nutritionService.analyzeIngredients(request);
    }

    @MutationMapping
    public NutritionResponse updateNutritionLog(@Argument Long logId, @Argument("request") Map<String, Object> requestMap) {
        NutritionRequest request = buildRequestFromMap(requestMap);
        return nutritionService.updateNutritionLog(logId, request);
    }
}