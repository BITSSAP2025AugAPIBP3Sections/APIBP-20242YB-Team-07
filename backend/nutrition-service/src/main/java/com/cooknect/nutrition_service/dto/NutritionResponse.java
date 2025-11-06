package com.cooknect.nutrition_service.dto;

import java.util.List;

import com.cooknect.nutrition_service.model.MealType;

public class NutritionResponse {

    private String userName;
    private Long recipeId;
    private String recipeName;
    private double totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
    private List<String> analyzedIngredients;
    private MealType mealType;

    // Default constructor
    public NutritionResponse() {}

    // Parameterized constructor
    public NutritionResponse(String userName, Long recipeId, String recipeName, double totalCalories, double totalProtein,
                             double totalCarbs, double totalFat, List<String> analyzedIngredients, MealType mealType) {
        this.userName = userName;
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.totalCarbs = totalCarbs;
        this.totalFat = totalFat;
        this.analyzedIngredients = analyzedIngredients;
        this.mealType = mealType;
    }

    // Getters and Setters
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }

    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public double getTotalCalories() { return totalCalories; }
    public void setTotalCalories(double totalCalories) { this.totalCalories = totalCalories; }

    public double getTotalProtein() { return totalProtein; }
    public void setTotalProtein(double totalProtein) { this.totalProtein = totalProtein; }

    public double getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(double totalCarbs) { this.totalCarbs = totalCarbs; }

    public double getTotalFat() { return totalFat; }
    public void setTotalFat(double totalFat) { this.totalFat = totalFat; }

    public List<String> getAnalyzedIngredients() { return analyzedIngredients; }
    public void setAnalyzedIngredients(List<String> analyzedIngredients) { this.analyzedIngredients = analyzedIngredients; }

    public MealType getMealType() {return mealType;}
    public void setMealType(MealType mealType) { this.mealType = mealType;}

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}