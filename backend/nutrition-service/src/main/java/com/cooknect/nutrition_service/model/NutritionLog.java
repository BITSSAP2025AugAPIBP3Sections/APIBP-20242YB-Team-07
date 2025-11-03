package com.cooknect.nutrition_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;
    private Long recipeId;
    private String foodName;
    private String ingredients;
    private Double calories;
    private Double protein;
    private Double carbohydrates;
    private Double fat;

    @Enumerated(EnumType.STRING)
    private MealType mealType;

    @Builder.Default
    private LocalDate analyzedAt = LocalDate.now();
}