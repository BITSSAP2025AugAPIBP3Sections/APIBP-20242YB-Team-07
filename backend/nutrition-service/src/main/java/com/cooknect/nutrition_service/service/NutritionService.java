package com.cooknect.nutrition_service.service;

import com.cooknect.nutrition_service.model.FoodItem;
import com.cooknect.nutrition_service.model.MealType;
import com.cooknect.nutrition_service.model.NutritionLog;
import com.cooknect.nutrition_service.repository.FoodItemRepository;
import com.cooknect.nutrition_service.repository.NutritionLogRepository;
// import com.cooknect.nutrition_service.service.RecipeServiceClient;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cooknect.nutrition_service.dto.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NutritionService {

    private final FoodItemRepository foodRepo;
    private final NutritionLogRepository logRepo;
    private final ExternalNutritionApiService externalApiService;
    private RecipeServiceClient recipeClient;

    private record NutritionTotals(double totalFat, double saturatedFat, double sodium, double potassium, double cholestrol, double carbohydrates, double fiber, double sugar) {}

    public NutritionService(FoodItemRepository foodRepo, 
                            NutritionLogRepository logRepo, 
                            ExternalNutritionApiService externalApiService, 
                            RecipeServiceClient recipeClient) {
        this.foodRepo = foodRepo;
        this.logRepo = logRepo;
        this.externalApiService = externalApiService;
        this.recipeClient = recipeClient;
    }

    public NutritionResponse analyzeIngredients(NutritionRequest request, String userName) {
        List<Map<String, String>> ingredients = request.getIngredients() != null ? request.getIngredients() : Collections.emptyList();
        String recipeName = request.getRecipeName();

        if (request.getRecipeId() != null){
            Optional<RecipeServiceClient.RecipeDto> maybe = recipeClient.getRecipeById(request.getRecipeId());
            if (maybe.isPresent()){
                RecipeServiceClient.RecipeDto r = maybe.get();
                recipeName = r.getTitle() != null ? r.getTitle() : recipeName;
                if (r.getIngredients() != null){
                    ingredients = r.getIngredients().stream()
                            .map(i -> Map.<String,String>of("name", i.getName(),
                                    "quantity", i.getQuantity() == null ? "1" : i.getQuantity()))
                            .collect(Collectors.toList());
                }
            }
        }
        
        // Use the refactored helper method
        NutritionTotals totals = calculateNutrition(ingredients);

        NutritionResponse response = new NutritionResponse(
                userName, // Use the secure userName from the header
                request.getRecipeId(),
                recipeName,
                totals.totalFat(),
                totals.saturatedFat(),
                totals.sodium(),
                totals.potassium(),
                totals.cholestrol(),
                totals.carbohydrates(),
                totals.fiber(),
                totals.sugar(),
                ingredients.stream().map(i -> i.get("name")).collect(Collectors.toList()),
                request.getMealType()
        );
        
        NutritionLog log = NutritionLog.builder()
                .userName(userName) // Use the secure userName from the header
                .recipeId(request.getRecipeId())
                .foodName(request.getRecipeName())
                .ingredients(String.join(", ", ingredients.stream().map(i -> i.get("name")).toList()))
                .totalFat(totals.totalFat())
                .totalSaturatedFat(totals.saturatedFat())
                .totalSodium(totals.sodium())
                .totalPotassium(totals.potassium())
                .totalCholestrol(totals.cholestrol())
                .totalCarbohydrates(totals.carbohydrates())
                .totalFiber(totals.fiber())
                .totalSugar(totals.sugar())
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

    private NutritionTotals calculateNutrition(List<Map<String, String>> ingredients) {
        double totalFat = 0.0, totalSaturatedFat = 0.0, totalSodium = 0.0,
               totalPotassium = 0.0, totalCholestrol = 0.0, totalCarbohydrates = 0.0,
               totalFiber = 0.0, totalSugar = 0.0;

        for (Map<String, String> ingredientMap : ingredients) {
            String name = ingredientMap.get("name");
            String quantityStr = ingredientMap.get("quantity");
            String servingSize = ingredientMap.getOrDefault("servingSize", quantityStr);
            double qty = extractNumericQuantity(quantityStr);
            
            boolean isFromExternalApi = false;
            Optional<FoodItem> foodOptional = foodRepo.findByFoodItemIgnoreCase(name);
            FoodItem food;

            if (foodOptional.isPresent()) {
                food = foodOptional.get();
            } else {
                String apiQuery = (servingSize != null && !servingSize.isBlank())
                        ? servingSize + " " + name
                        : name;
                food = externalApiService.fetchNutritionInfo(apiQuery)
                    .map(foodRepo::save)
                    .orElse(null);
                isFromExternalApi = (food != null);
            }

            if (food != null) {
                if (isFromExternalApi) {
                    totalFat += safeNullableDouble(food.getTotalFat());
                    totalSaturatedFat += safeNullableDouble(food.getSaturatedFat());
                    totalSodium += safeNullableDouble(food.getSodium());
                    totalPotassium += safeNullableDouble(food.getPotassium());
                    totalCholestrol += safeNullableDouble(food.getCholestrol());
                    totalCarbohydrates += safeNullableDouble(food.getCarbohydrates());
                    totalFiber += safeNullableDouble(food.getFiber());
                    totalSugar += safeNullableDouble(food.getSugar());
                } else {
                    totalFat += safeNullableDouble(food.getTotalFat()) * qty;
                    totalSaturatedFat += safeNullableDouble(food.getSaturatedFat()) * qty;
                    totalSodium += safeNullableDouble(food.getSodium()) * qty;
                    totalPotassium += safeNullableDouble(food.getPotassium()) * qty;
                    totalCholestrol += safeNullableDouble(food.getCholestrol()) * qty;
                    totalCarbohydrates += safeNullableDouble(food.getCarbohydrates()) * qty;
                    totalFiber += safeNullableDouble(food.getFiber()) * qty;
                    totalSugar += safeNullableDouble(food.getSugar()) * qty;
                }
            } else {
                totalFat += 2 * qty;
                totalSaturatedFat += 0.5 * qty;
                totalSodium += 150 * qty;
                totalPotassium += 100 * qty;
                totalCholestrol += 30 * qty;
                totalCarbohydrates += 10 * qty;
                totalFiber += 1 * qty;
                totalSugar += 5 * qty;
            }
        }
        return new NutritionTotals(totalFat, totalSaturatedFat, totalSodium, totalPotassium, totalCholestrol, totalCarbohydrates, totalFiber, totalSugar);
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

    public List<NutritionLog> getNutritionLogsByMealType(String userName, MealType mealType) {
        return logRepo.findByUserNameAndMealType(userName, mealType);
    }

    public DailyIntakeSummary getTodayIntakeSummary(String userName) {
        LocalDate today = LocalDate.now();
        List<NutritionLog> todayLogs = logRepo.findByUserNameAndAnalyzedAt(userName, today);
        double totalFat = todayLogs.stream().mapToDouble(NutritionLog::getTotalFat).sum();
        double totalSaturatedFat = todayLogs.stream().mapToDouble(NutritionLog::getTotalSaturatedFat).sum();
        double totalSodium = todayLogs.stream().mapToDouble(NutritionLog::getTotalSodium).sum();
        double totalPotassium = todayLogs.stream().mapToDouble(NutritionLog::getTotalPotassium).sum();
        double totalCholestrol = todayLogs.stream().mapToDouble(NutritionLog::getTotalCholestrol).sum();
        double totalCarbohydrates = todayLogs.stream().mapToDouble(NutritionLog::getTotalCarbohydrates).sum();
        double totalFiber = todayLogs.stream().mapToDouble(NutritionLog::getTotalFiber).sum();
        double totalSugar = todayLogs.stream().mapToDouble(NutritionLog::getTotalSugar).sum();
        return new DailyIntakeSummary(totalFat, totalSaturatedFat, totalSodium, totalPotassium, totalCholestrol, totalCarbohydrates, totalFiber, totalSugar); 
    }

    public NutritionResponse updateNutritionLog(Long logId, NutritionRequest request, String userName) {
        NutritionLog log = logRepo.findById(logId)
                .orElseThrow(() -> new RuntimeException("Nutrition log not found"));

        // Add authorization check
        if (!log.getUserName().equals(userName)) {
            throw new RuntimeException("User not authorized to update this log");
        }

        List<Map<String, String>> ingredients = request.getIngredients() != null ? request.getIngredients() : Collections.emptyList();
        NutritionTotals totals = calculateNutrition(ingredients);

        log.setRecipeId(request.getRecipeId());
        log.setFoodName(request.getRecipeName());
        log.setIngredients(String.join(", ", ingredients.stream().map(i -> i.get("name")).toList()));
        log.setTotalFat(totals.totalFat());
        log.setTotalSaturatedFat(totals.saturatedFat());
        log.setTotalSodium(totals.sodium());
        log.setTotalPotassium(totals.potassium());
        log.setTotalCholestrol(totals.cholestrol());
        log.setTotalCarbohydrates(totals.carbohydrates());
        log.setTotalFiber(totals.fiber());
        log.setTotalSugar(totals.sugar());
        log.setMealType(request.getMealType());
        log.setAnalyzedAt(LocalDate.now());

        logRepo.save(log);

        return new NutritionResponse(
                log.getUserName(),
                log.getRecipeId(),
                log.getFoodName(),
                log.getTotalFat(),
                log.getTotalSaturatedFat(),
                log.getTotalSodium(),
                log.getTotalPotassium(),
                log.getTotalCholestrol(),
                log.getTotalCarbohydrates(),
                log.getTotalFiber(),
                log.getTotalSugar(),
                ingredients.stream().map(i -> i.get("name")).toList(),
                log.getMealType()
        );
    }

    public NutritionLog patchNutritionLog(Long logId, Map<String, Object> updates, String userName) {
        NutritionLog log = logRepo.findById(logId)
                .orElseThrow(() -> new RuntimeException("Nutrition log not found"));

        // Add authorization check
        if (!log.getUserName().equals(userName)) {
            throw new RuntimeException("User not authorized to patch this log");
        }

        if (updates.containsKey("mealType")) {
            log.setMealType(MealType.valueOf((String) updates.get("mealType")));
        }

        return logRepo.save(log);
    }

    public void deleteNutritionLog(Long logId, String userName) {
        NutritionLog log = logRepo.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutrition log not found with id: " + logId));
        

        if (!log.getUserName().equals(userName)) {
            throw new RuntimeException("User not authorized to delete this log");
        }
        
        logRepo.delete(log);
    }

    private double safeNullableDouble(Double v) {
        return v == null ? 0.0 : v.doubleValue();
    }
}