package com.cooknect.nutrition_service.controller;

import com.cooknect.nutrition_service.dto.NutritionRequest;
import com.cooknect.nutrition_service.dto.NutritionResponse;
import com.cooknect.nutrition_service.model.NutritionLog;
import com.cooknect.nutrition_service.model.FoodItem;
import com.cooknect.nutrition_service.model.MealType;
import com.cooknect.nutrition_service.service.NutritionService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    @Autowired
    private NutritionService nutritionService;

    @PostMapping("/addFoodItem")
    public ResponseEntity<FoodItem> addFoodItem(@RequestBody FoodItem foodItem) {
        FoodItem createdFoodItem = nutritionService.addFoodItem(foodItem);
        return ResponseEntity.ok(createdFoodItem);
    }

    @GetMapping("/allFoodItems")
    public ResponseEntity<List<FoodItem>> getAllFoodItems() {
        return ResponseEntity.ok(nutritionService.getAllFoodItems());
    }

    @PostMapping("/analyze")
    public ResponseEntity<NutritionResponse> analyze(@RequestBody NutritionRequest req) {
        return ResponseEntity.ok(nutritionService.analyzeIngredients(req));
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Nutrition Service is up and running!");
    }

    @GetMapping("/allNutritionLogs")
    public ResponseEntity<List<NutritionLog>> getAllNutritionLogs() {
        return ResponseEntity.ok(nutritionService.getAllNutritionLogs());
    }

    @GetMapping("/NutritionLogsByUserId/{userName}")
    public ResponseEntity<List<NutritionLog>> getNutritionLogsByUserName(@PathVariable String userName) {
        return ResponseEntity.ok(nutritionService.getNutritionLogsByUserName(userName));
    }

    @GetMapping("/user/{userName}/MealTypes/{mealType}")
    public ResponseEntity<List<NutritionLog>> getNutritionLogsByMealType(@PathVariable String userName, @PathVariable MealType mealType) {
        return ResponseEntity.ok(nutritionService.getNutritionLogsByMealType(userName, mealType));
    }

    @PutMapping("/log/{logId}")
    public ResponseEntity<NutritionResponse> updateNutritionLog(
            @PathVariable Long logId,
            @RequestBody NutritionRequest request) {
        return ResponseEntity.ok(nutritionService.updateNutritionLog(logId, request));
    }

    @PatchMapping("/log/{logId}")
    public ResponseEntity<NutritionLog> patchNutritionLog(@PathVariable Long logId, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(nutritionService.patchNutritionLog(logId, updates));
    }

    @DeleteMapping("/log/{logId}")
    public ResponseEntity<Void> deleteNutritionLog(@PathVariable Long logId) {
        nutritionService.deleteNutritionLog(logId);
        return ResponseEntity.noContent().build();  
    }
}