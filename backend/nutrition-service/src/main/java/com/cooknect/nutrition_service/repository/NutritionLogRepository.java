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
    List<NutritionLog> findByUserName(String userName);
    List<NutritionLog> findByUserNameAndMealType(String userName, MealType mealType);
    List<NutritionLog> findByUserNameAndAnalyzedAt(String userName, LocalDate analyzedAt);
    List<NutritionLog> findByUserNameAndMealTypeAndAnalyzedAt(String userName, MealType mealType, LocalDate analyzedAt);
    List<NutritionLog> findByUserNameAndAnalyzedAtBetween(String userName, LocalDate startDate, LocalDate endDate);
}