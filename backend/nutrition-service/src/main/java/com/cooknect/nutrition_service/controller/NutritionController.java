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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private static final Logger logger = LoggerFactory.getLogger(NutritionController.class);

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
    public ResponseEntity<NutritionResponse> analyze(@RequestBody NutritionRequest req, @RequestHeader("X-User-Id") Long userId) {
        logger.info("Analyzing ingredients for user ID: {}", userId);
        req.setUserId(userId);
        return ResponseEntity.ok(nutritionService.analyzeIngredients(req, userId));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check endpoint called");
        return ResponseEntity.ok("Nutrition Service is up and running!");
    }

    @GetMapping("/allNutritionLogs")
    @Operation(summary = "Get all nutrition logs", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NutritionLog>> getAllNutritionLogs() {
        logger.info("Fetching all nutrition logs");
        return ResponseEntity.ok(nutritionService.getAllNutritionLogs());
    }

    @GetMapping("/NutritionLogsByUserId/{userId}")
    @Operation(summary = "Get nutrition logs by user ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NutritionLog>> getNutritionLogsByUserId(@PathVariable Long userId) {
        logger.info("Fetching nutrition logs for user ID: {}", userId);
        return ResponseEntity.ok(nutritionService.getNutritionLogsByUserId(userId));
    }

    @GetMapping("/user/{userId}/MealTypes/{mealType}")
    @Operation(summary = "Get nutrition logs by meal type", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<NutritionLog>> getNutritionLogsByMealType(@RequestHeader("X-User-Id") Long userId, @PathVariable MealType mealType) {
        logger.info("Fetching nutrition logs for user ID: {} and meal type: {}", userId, mealType);
        return ResponseEntity.ok(nutritionService.getNutritionLogsByMealType(userId, mealType));
    }

    @GetMapping("/today/summary")
    @Operation(summary = "Get today's nutrition intake summary for a user", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<DailyIntakeSummary> getTodayIntakeSummary(@RequestHeader("X-User-Id") Long userId) {
        logger.info("Fetching today's nutrition intake summary for user ID: {}", userId);
        DailyIntakeSummary summary = nutritionService.getTodayIntakeSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/log/{logId}")
    @Operation(summary = "Update nutrition log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<NutritionResponse> updateNutritionLog(
            @PathVariable Long logId,
            @RequestBody NutritionRequest request,
            @RequestHeader("X-User-Id") long userId) {
        logger.info("Updating nutrition log ID: {} for user ID: {}", logId, userId);
        return ResponseEntity.ok(nutritionService.updateNutritionLog(logId, request, userId));
    }

    @PatchMapping("/log/{logId}")
    @Operation(summary = "Patch nutrition log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<NutritionLog> patchNutritionLog(@PathVariable Long logId,
                                                          @RequestBody Map<String, Object> updates,
                                                          @RequestHeader("X-User-Id") Long userId) {
        logger.info("Patching nutrition log ID: {} for user ID: {}", logId, userId);
        return ResponseEntity.ok(nutritionService.patchNutritionLog(logId, updates, userId));
    }

    @DeleteMapping("/log/{logId}")
    @Operation(summary = "Delete nutrition log", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> deleteNutritionLog(@PathVariable Long logId,
                                                   @RequestHeader("X-User-Id") Long userId) {
        logger.info("Deleting nutrition log ID: {} for user ID: {}", logId, userId);
        nutritionService.deleteNutritionLog(logId, userId);
        return ResponseEntity.ok(Map.of("message", "Nutrition log deleted successfully"));
    }
}