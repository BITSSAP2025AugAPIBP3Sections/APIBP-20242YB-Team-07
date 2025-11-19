package com.cooknect.nutrition_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "nutrition_log_user_recipe_meal_analyzed_key", 
        columnNames = {"user_id", "recipeId", "analyzedAt", "mealType"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private Long recipeId;

    private String foodName;
    private String ingredients;

    private double totalFat;
    private double totalSaturatedFat;
    private double totalSodium;
    private double totalPotassium;
    private double totalCholestrol;
    private double totalCarbohydrates;
    private double totalFiber;
    private double totalSugar;

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    @Builder.Default
    private LocalDate analyzedAt = LocalDate.now();
}