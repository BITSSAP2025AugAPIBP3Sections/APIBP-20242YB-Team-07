package com.cooknect.nutrition_service.repository;

import com.cooknect.nutrition_service.model.MealType;
import com.cooknect.nutrition_service.model.NutritionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface NutritionLogRepository extends JpaRepository<NutritionLog, Long> {
    Optional<NutritionLog> findByFoodName(String foodName);
    Optional<NutritionLog> findByRecipeId(Long recipeId);
    List<NutritionLog> findByUserId(Long userId);
    List<NutritionLog> findByUserIdAndMealType(long userId, MealType mealType);
    List<NutritionLog> findByUserIdAndAnalyzedAt(Long userId, LocalDate analyzedAt);
    List<NutritionLog> findByUserIdAndMealTypeAndAnalyzedAt(Long userId, MealType mealType, LocalDate analyzedAt);
    List<NutritionLog> findByUserIdAndAnalyzedAtBetween(Long userId, LocalDate startDate, LocalDate endDate);
}