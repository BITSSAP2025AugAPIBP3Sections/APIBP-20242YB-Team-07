package com.cooknect.nutrition_service.controller;

import com.cooknect.nutrition_service.dto.DailyIntakeSummary;
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

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    @Autowired
    private NutritionService nutritionService;

    @PostMapping("/food-item")
    @Operation(summary = "Add a new food item", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<FoodItem> addFoodItem(@RequestBody FoodItem foodItem) {
        FoodItem createdFoodItem = nutritionService.addFoodItem(foodItem);
        return ResponseEntity.ok(createdFoodItem);
    }

    @GetMapping("/food-items")
    @Operation(summary = "Get all food items", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<FoodItem>> getAllFoodItems() {
        return ResponseEntity.ok(nutritionService.getAllFoodItems());
    }

    @PostMapping("/")
    @Operation(summary = "Analyze food ingredients", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<NutritionResponse> analyze(@RequestBody NutritionRequest req, @RequestHeader("X-User-Id") Long userId) {
        req.setUserId(userId);
        return ResponseEntity.ok(nutritionService.analyzeIngredients(req, userId));
    }

    @GetMapping("/Logs")
    @Operation(summary = "Get all nutrition logs", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NutritionLog>> getAllNutritionLogs() {
        return ResponseEntity.ok(nutritionService.getAllNutritionLogs());
    }

    @GetMapping("/NutritionLogsByUserId")
    @Operation(summary = "Get nutrition logs by user ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NutritionLog>> getNutritionLogsByUserId(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(nutritionService.getNutritionLogsByUserId(userId));
    }

    @GetMapping("/MealTypes/{mealType}")
    @Operation(summary = "Get nutrition logs by meal type", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NutritionLog>> getNutritionLogsByMealType(@RequestHeader("X-User-Id") Long userId, @PathVariable MealType mealType) {
        return ResponseEntity.ok(nutritionService.getNutritionLogsByMealType(userId, mealType));
    }

    @GetMapping({"/summary", "/summary/{date}"})
    @Operation(summary = "Get today's nutrition intake summary for a user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DailyIntakeSummary> getTodayIntakeSummary(@RequestHeader("X-User-Id") Long userId, @PathVariable(required = false) String date) {
        LocalDate targetDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        DailyIntakeSummary summary = nutritionService.getTodayIntakeSummary(userId, targetDate);
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/logs/{logId}")
    @Operation(summary = "Update nutrition log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<NutritionResponse> updateNutritionLog(
            @PathVariable Long logId,
            @RequestBody NutritionRequest request,
            @RequestHeader("X-User-Id") long userId) {
        return ResponseEntity.ok(nutritionService.updateNutritionLog(logId, request, userId));
    }

    @PatchMapping("/logs/{logId}")
    @Operation(summary = "Patch nutrition log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<NutritionLog> patchNutritionLog(@PathVariable Long logId,
                                                          @RequestBody Map<String, Object> updates,
                                                          @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(nutritionService.patchNutritionLog(logId, updates, userId));
    }

    @DeleteMapping("/logs/{logId}")
    @Operation(summary = "Delete nutrition log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> deleteNutritionLog(@PathVariable Long logId,
                                                                  @RequestHeader("X-User-Id") Long userId) {
        nutritionService.deleteNutritionLog(logId, userId);
        return ResponseEntity.ok(Map.of("message", "Nutrition log deleted successfully"));
    }
}