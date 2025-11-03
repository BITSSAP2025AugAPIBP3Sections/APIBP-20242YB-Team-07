package com.cooknect.nutrition_service.service;

import com.cooknect.nutrition_service.model.FoodItem;
import com.cooknect.nutrition_service.model.MealType;
import com.cooknect.nutrition_service.model.NutritionLog;
import com.cooknect.nutrition_service.repository.FoodItemRepository;
import com.cooknect.nutrition_service.repository.NutritionLogRepository;
import org.springframework.stereotype.Service;

import com.cooknect.nutrition_service.dto.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@Service
public class NutritionService {

    private final FoodItemRepository foodRepo;
    private final NutritionLogRepository logRepo;

    public NutritionService(FoodItemRepository foodRepo, NutritionLogRepository logRepo) {
        this.foodRepo = foodRepo;
        this.logRepo = logRepo;
    }

    public NutritionResponse analyzeIngredients(NutritionRequest request) {
        double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;

        for (Map<String, String> ingredientMap : request.getIngredients()) {
            String name = ingredientMap.get("name");
            String quantityStr = ingredientMap.get("quantity");
            String servingSize = ingredientMap.get("servingSize");
            double qty = extractNumericQuantity(quantityStr);

            FoodItem food = foodRepo.findByFoodItemIgnoreCase(name).orElse(null);

            if (food != null) {
                if (servingSize != null && !servingSize.isEmpty()
                        && (food.getServingSize() == null || !food.getServingSize().equalsIgnoreCase(servingSize))) {
                    food.setServingSize(servingSize);
                    foodRepo.save(food);
                }

                totalCalories += food.getCalories() * qty;
                totalProtein += food.getProtein() * qty;
                totalCarbs += food.getCarbohydrates() * qty;
                totalFat += food.getFat() * qty;
            } else {
                totalCalories += 50 * qty;
                totalProtein += 1 * qty;
                totalCarbs += 10 * qty;
                totalFat += 2 * qty;
            }
        }
        NutritionResponse response = new NutritionResponse(
                request.getUserName(),
                request.getRecipeId(),
                request.getRecipeName(),
                totalCalories,
                totalProtein,
                totalCarbs,
                totalFat,
                request.getIngredients().stream()
                        .map(i -> i.get("name"))
                        .toList(),
                request.getMealType()
        );

        // ✅ Save to nutrition_log table
        NutritionLog log = NutritionLog.builder()
                .userName(request.getUserName())
                .recipeId(request.getRecipeId())
                .foodName(request.getRecipeName())
                .ingredients(String.join(", ",
                        request.getIngredients().stream()
                                .map(i -> i.get("name"))
                                .toList()))
                .calories(totalCalories)
                .protein(totalProtein)
                .carbohydrates(totalCarbs)
                .fat(totalFat)
                .mealType(request.getMealType())
                .analyzedAt(LocalDate.now())
                .build();

        logRepo.save(log);

        return response;
    }

    private double extractNumericQuantity(String quantity) {
        if (quantity == null || quantity.isEmpty()) return 1.0;

        try {
            String[] parts = quantity.split(" ");
            String numeric = parts[0].replaceAll("[^0-9.]", "");
            return Double.parseDouble(numeric);
        } catch (Exception e) {
            return 1.0;
        }
    }

    public List<NutritionLog> getAllNutritionLogs() {
        return logRepo.findAll();
    }

    public List<FoodItem> getAllFoodItems() {
        return foodRepo.findAll();
    }

    public FoodItem addFoodItem(FoodItem foodItem) {
        return foodRepo.save(foodItem);
    }

    public List<NutritionLog> getNutritionLogsByUserName(String userName) {
        return logRepo.findByUserName(userName);
    }

    public FoodItemRepository getFoodRepo() {
        return foodRepo;
    }

    public List<NutritionLog> getNutritionLogsByMealType(String userName, MealType mealType) {
        return logRepo.findByUserNameAndMealType(userName, mealType);
    }

    public DailyIntakeSummary getTodayIntakeSummary(String userName) {
        LocalDate today = LocalDate.now();
        List<NutritionLog> todayLogs = logRepo.findByUserNameAndAnalyzedAt(userName, today);

        double totalCalories = todayLogs.stream().mapToDouble(NutritionLog::getCalories).sum();
        double totalProtein = todayLogs.stream().mapToDouble(NutritionLog::getProtein).sum();
        double totalCarbs = todayLogs.stream().mapToDouble(NutritionLog::getCarbohydrates).sum();
        double totalFat = todayLogs.stream().mapToDouble(NutritionLog::getFat).sum();

        return new DailyIntakeSummary(totalCalories, totalProtein, totalCarbs, totalFat); 
    }

    public NutritionResponse updateNutritionLog(Long logId, NutritionRequest request) {
        NutritionLog log = logRepo.findById(logId)
                .orElseThrow(() -> new RuntimeException("Nutrition log not found"));

        double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;

        for (Map<String, String> ingredientMap : request.getIngredients()) {
            String name = ingredientMap.get("name");
            String quantityStr = ingredientMap.get("quantity");
            String servingSize = ingredientMap.get("servingSize");
            double qty = extractNumericQuantity(quantityStr);

            FoodItem food = foodRepo.findByFoodItemIgnoreCase(name).orElse(null);

            if (food != null) {
                if (servingSize != null && !servingSize.isEmpty()
                        && (food.getServingSize() == null || !food.getServingSize().equalsIgnoreCase(servingSize))) {
                    food.setServingSize(servingSize);
                    foodRepo.save(food);
                }

                totalCalories += food.getCalories() * qty;
                totalProtein += food.getProtein() * qty;
                totalCarbs += food.getCarbohydrates() * qty;
                totalFat += food.getFat() * qty;
            } else {
                totalCalories += 50 * qty;
                totalProtein += 1 * qty;
                totalCarbs += 10 * qty;
                totalFat += 2 * qty;
            }
        }

        log.setRecipeId(request.getRecipeId());
        log.setFoodName(request.getRecipeName());
        log.setIngredients(String.join(", ",
                request.getIngredients().stream()
                        .map(i -> i.get("name"))
                        .toList()));
        log.setCalories(totalCalories);
        log.setProtein(totalProtein);
        log.setCarbohydrates(totalCarbs);
        log.setFat(totalFat);
        log.setMealType(request.getMealType());
        log.setAnalyzedAt(LocalDate.now());

        logRepo.save(log);

        return new NutritionResponse(
                log.getUserName(),
                log.getRecipeId(),
                log.getFoodName(),
                log.getCalories(),
                log.getProtein(),
                log.getCarbohydrates(),
                log.getFat(),
                request.getIngredients().stream()
                        .map(i -> i.get("name"))
                        .toList(),
                log.getMealType()
        );
    }
}