package com.cooknect.nutrition_service.repository;

import com.cooknect.nutrition_service.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    Optional<FoodItem> findByFoodItemIgnoreCase(String foodItem);
}
