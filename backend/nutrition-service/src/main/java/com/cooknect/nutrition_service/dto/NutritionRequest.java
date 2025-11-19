package com.cooknect.nutrition_service.dto;

import java.util.List;
import java.util.Map;

import com.cooknect.nutrition_service.model.MealType;

public class NutritionRequest {

    private Long recipeId;
    private String recipeName;
    private List<Map<String, String>> ingredients;
    private MealType mealType = MealType.BREAKFAST;
    private Long userId;

    public NutritionRequest() {
    }

    public NutritionRequest(Long recipeId, String recipeName, List<Map<String, String>> ingredients, MealType mealType) {
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.ingredients = ingredients;
        this.mealType = (mealType != null) ? mealType : MealType.BREAKFAST;
    }

    public Long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(Long recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public List<Map<String, String>> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Map<String, String>> ingredients) {
        this.ingredients = ingredients;
    }

    public MealType getMealType() { 
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = (mealType != null) ? mealType : MealType.BREAKFAST;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}