package com.cooknect.nutrition_service.controller;

import com.cooknect.nutrition_service.dto.NutritionRequest;
import com.cooknect.nutrition_service.dto.NutritionResponse;
import com.cooknect.nutrition_service.model.NutritionLog;
import com.cooknect.nutrition_service.model.FoodItem;
import com.cooknect.nutrition_service.model.MealType;
import com.cooknect.nutrition_service.service.NutritionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @Operation(summary = "Add a new food item", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<FoodItem> addFoodItem(@RequestBody FoodItem foodItem) {
        FoodItem createdFoodItem = nutritionService.addFoodItem(foodItem);
        return ResponseEntity.ok(createdFoodItem);
    }

    @GetMapping("/allFoodItems")
    @Operation(summary = "Get all food items", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<FoodItem>> getAllFoodItems() {
        return ResponseEntity.ok(nutritionService.getAllFoodItems());
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyze food ingredients", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<NutritionResponse> analyze(@RequestBody NutritionRequest req) {
        return ResponseEntity.ok(nutritionService.analyzeIngredients(req));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Nutrition Service is up and running!");
    }

    @GetMapping("/allNutritionLogs")
    @Operation(summary = "Get all nutrition logs", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NutritionLog>> getAllNutritionLogs() {
        return ResponseEntity.ok(nutritionService.getAllNutritionLogs());
    }

    @GetMapping("/NutritionLogsByUserId/{userName}")
    @Operation(summary = "Get nutrition logs by user name", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NutritionLog>> getNutritionLogsByUserName(@PathVariable String userName) {
        return ResponseEntity.ok(nutritionService.getNutritionLogsByUserName(userName));
    }

    @GetMapping("/user/{userName}/MealTypes/{mealType}")
    @Operation(summary = "Get nutrition logs by meal type", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NutritionLog>> getNutritionLogsByMealType(@PathVariable String userName, @PathVariable MealType mealType) {
        return ResponseEntity.ok(nutritionService.getNutritionLogsByMealType(userName, mealType));
    }

    @PutMapping("/log/{logId}")
    @Operation(summary = "Update nutrition log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<NutritionResponse> updateNutritionLog(
            @PathVariable Long logId,
            @RequestBody NutritionRequest request,
            @RequestHeader("X-User-Name") String userName) {
        return ResponseEntity.ok(nutritionService.updateNutritionLog(logId, request, userName));
    }

    @PatchMapping("/log/{logId}")
    @Operation(summary = "Patch nutrition log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<NutritionLog> patchNutritionLog(@PathVariable Long logId, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(nutritionService.patchNutritionLog(logId, updates));
    }

    @DeleteMapping("/log/{logId}")
    @Operation(summary = "Delete nutrition log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteNutritionLog(@PathVariable Long logId) {
        nutritionService.deleteNutritionLog(logId);
        return ResponseEntity.noContent().build();  
    }
}