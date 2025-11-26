package com.cooknect.nutrition_service.dto;

import java.util.List;

import com.cooknect.nutrition_service.model.MealType;

public class NutritionResponse {

    private Long userId;
    private Long recipeId;
    private String recipeName;
    private double totalFat;
    private double totalSodium;
    private double totalPotassium;
    private double totalCholestrol;
    private double totalCarbohydrates;
    private double totalFiber;
    private double totalSugar;
    private List<String> analyzedIngredients;
    private MealType mealType;

    // Default constructor
    public NutritionResponse() {}

    // Parameterized constructor
    public NutritionResponse(Long userId,
                            Long recipeId, 
                            String recipeName, 
                            double totalFat,
                            double totalSodium,
                            double totalPotassium,
                            double totalCholestrol,
                            double totalCarbohydrates,
                            double totalFiber,
                            double totalSugar,
                            List<String> analyzedIngredients, 
                            MealType mealType) {
        this.userId = userId;
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.totalFat = totalFat;
        this.totalSodium = totalSodium;
        this.totalPotassium = totalPotassium;
        this.totalCholestrol = totalCholestrol;
        this.totalCarbohydrates = totalCarbohydrates;
        this.totalFiber = totalFiber;
        this.totalSugar = totalSugar;
        this.analyzedIngredients = analyzedIngredients;
        this.mealType = mealType;
    }

    // Getters and Setters
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }

    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public double getTotalFat() { return totalFat; }
    public void setTotalFat(double totalFat) { this.totalFat = totalFat; }

    public double getTotalSodium() { return totalSodium; }
    public void setTotalSodium(double totalSodium) { this.totalSodium = totalSodium; }

    public double getTotalPotassium() { return totalPotassium; }
    public void setTotalPotassium(double totalPotassium) { this.totalPotassium = totalPotassium; }

    public double getTotalCholestrol() { return totalCholestrol; }
    public void setTotalCholestrol(double totalCholestrol) { this.totalCholestrol = totalCholestrol; }

    public double getTotalCarbohydrates() { return totalCarbohydrates; }
    public void setTotalCarbohydrates(double totalCarbohydrates) { this.totalCarbohydrates = totalCarbohydrates; }

    public double getTotalFiber() { return totalFiber; }
    public void setTotalFiber(double totalFiber) { this.totalFiber = totalFiber; }

    public double getTotalSugar() { return totalSugar; }
    public void setTotalSugar(double totalSugar) { this.totalSugar = totalSugar; }

    public List<String> getAnalyzedIngredients() { return analyzedIngredients; }
    public void setAnalyzedIngredients(List<String> analyzedIngredients) { this.analyzedIngredients = analyzedIngredients; }

    public MealType getMealType() {return mealType;}
    public void setMealType(MealType mealType) { this.mealType = mealType;}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}