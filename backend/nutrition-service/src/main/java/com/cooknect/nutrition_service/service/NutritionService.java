package com.cooknect.nutrition_service.service;

import com.cooknect.nutrition_service.model.FoodItem;
import com.cooknect.nutrition_service.model.MealType;
import com.cooknect.nutrition_service.model.NutritionLog;
import com.cooknect.nutrition_service.repository.FoodItemRepository;
import com.cooknect.nutrition_service.repository.NutritionLogRepository;
import com.cooknect.nutrition_service.service.RecipeGrpcClient;
import com.recipe.RecipeResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cooknect.nutrition_service.dto.*;

import org.slf4j.*;

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
    private final RecipeGrpcClient recipeGrpcClient;

    private final Logger logger = LoggerFactory.getLogger(NutritionService.class);

    private record NutritionTotals(double totalFat, double sodium, double potassium, double cholestrol, double carbohydrates, double fiber, double sugar) {}

    public NutritionService(FoodItemRepository foodRepo,
                            NutritionLogRepository logRepo,
                            ExternalNutritionApiService externalApiService,
                            RecipeGrpcClient recipeGrpcClient) {
        this.foodRepo = foodRepo;
        this.logRepo = logRepo;
        this.externalApiService = externalApiService;
        this.recipeGrpcClient = recipeGrpcClient;
    }

    public NutritionResponse analyzeIngredients(NutritionRequest request, Long userId) {
        logger.debug("Analyzing ingredients for user ID: {}", userId);
        List<Map<String, String>> ingredients = request.getIngredients() != null ? request.getIngredients() : new java.util.ArrayList<>();
        String recipeName = request.getRecipeName();

        if (request.getRecipeId() != null){
            logger.debug("Fetching recipe via gRPC for recipe ID: {}", request.getRecipeId());
            Optional<RecipeResponse> recipeMaybe = recipeGrpcClient.getRecipeById(request.getRecipeId());
            if (recipeMaybe.isPresent()){
                RecipeResponse recipe = recipeMaybe.get();
                recipeName = !recipe.getTitle().isEmpty() ? recipe.getTitle() : recipeName;
                if (!recipe.getIngredientsList().isEmpty()){
                    ingredients = recipe.getIngredientsList().stream()
                            .map(i -> Map.of("name", i.getName(),
                                    "quantity", !i.getQuantity().isEmpty() ? i.getQuantity() : "1"))
                            .collect(Collectors.toList());
                    logger.info("✅ Fetched {} ingredients from gRPC for recipe: {}", ingredients.size(), recipeName);
                }
            }
            else{
                logger.warn("❌ Recipe not found via gRPC for ID: {}", request.getRecipeId());
            }
        }

        // Use the refactored helper method
        NutritionTotals totals = calculateNutrition(ingredients);

        NutritionResponse response = new NutritionResponse(
                userId,
                request.getRecipeId(),
                recipeName,
                totals.totalFat(),
                totals.sodium(),
                totals.potassium(),
                totals.cholestrol(),
                totals.carbohydrates(),
                totals.fiber(),
                totals.sugar(),
                ingredients.stream().map(i -> i.get("name")).collect(Collectors.toList()),
                request.getMealType()
        );

        // Check if a log already exists for this user, recipe, date, and meal type
        LocalDate today = LocalDate.now();
        Optional<NutritionLog> existingLog = logRepo.findByUserIdAndRecipeIdAndAnalyzedAtAndMealType(
                userId, request.getRecipeId(), today, request.getMealType());

        NutritionLog log;
        if (existingLog.isPresent()) {
            // Update existing log
            log = existingLog.get();
            logger.info("Updating existing nutrition log ID: {} for user: {}, recipe: {}, meal: {}",
                    log.getId(), userId, request.getRecipeId(), request.getMealType());
            log.setFoodName(request.getRecipeName());
            log.setIngredients(String.join(", ", ingredients.stream().map(i -> i.get("name")).toList()));
            log.setTotalFat(totals.totalFat());
            log.setTotalSodium(totals.sodium());
            log.setTotalPotassium(totals.potassium());
            log.setTotalCholestrol(totals.cholestrol());
            log.setTotalCarbohydrates(totals.carbohydrates());
            log.setTotalFiber(totals.fiber());
            log.setTotalSugar(totals.sugar());
        } else {
            // Create new log
            log = NutritionLog.builder()
                    .userId(userId)
                    .recipeId(request.getRecipeId())
                    .foodName(request.getRecipeName())
                    .ingredients(String.join(", ", ingredients.stream().map(i -> i.get("name")).toList()))
                    .totalFat(totals.totalFat())
                    .totalSodium(totals.sodium())
                    .totalPotassium(totals.potassium())
                    .totalCholestrol(totals.cholestrol())
                    .totalCarbohydrates(totals.carbohydrates())
                    .totalFiber(totals.fiber())
                    .totalSugar(totals.sugar())
                    .mealType(request.getMealType())
                    .analyzedAt(today)
                    .build();
            logger.info("Creating new nutrition log for user: {}, recipe: {}, meal: {}",
                    userId, request.getRecipeId(), request.getMealType());
        }

        logRepo.save(log);
        logger.info("SUCCESS");
        return response;
    }

    private double extractNumericQuantity(String quantity) {
        if (quantity == null || quantity.isEmpty()) {
            logger.warn("Quantity is null or empty, defaulting to 1.0");
            return 1.0;
        }
        try {
            String[] parts = quantity.split(" ");
            String numeric = parts[0].replaceAll("[^0-9.]", "");
            return Double.parseDouble(numeric);
        } catch (Exception e) {
            return 1.0;
        }
    }

    /**
     * Build a better query for external nutrition API
     * Converts: "200" + "Paneer" → "200g paneer"
     *          "3" + "Tomato" → "3 medium tomato"
     *          "2" + "Butter" → "2 tbsp butter"
     */
    private String buildApiQuery(String name, String quantity) {
        if (quantity == null || quantity.isEmpty()) {
            return name;
        }

        // If quantity already has units (g, kg, ml, tbsp, cup, etc.), use as is
        if (quantity.matches(".*[a-zA-Z]+.*")) {
            return quantity + " " + name;
        }

        // Otherwise, add appropriate units based on ingredient name
        String lowerName = name.toLowerCase();

        // Solid ingredients - add grams
        if (lowerName.contains("paneer") || lowerName.contains("cheese") ||
            lowerName.contains("butter") || lowerName.contains("meat") ||
            lowerName.contains("chicken") || lowerName.contains("fish")) {
            return quantity + "g " + name;
        }

        // Liquids - add ml
        if (lowerName.contains("cream") || lowerName.contains("milk") ||
            lowerName.contains("water") || lowerName.contains("oil")) {
            return quantity + "ml " + name;
        }

        // Spices and powders - add tsp
        if (lowerName.contains("masala") || lowerName.contains("chili") ||
            lowerName.contains("powder") || lowerName.contains("spice")) {
            return quantity + " tsp " + name;
        }

        // Vegetables/fruits - add "medium" or "large"
        if (lowerName.contains("tomato") || lowerName.contains("onion") ||
            lowerName.contains("potato") || lowerName.contains("apple")) {
            return quantity + " medium " + name;
        }

        // Default - just add quantity with name
        return quantity + " " + name;
    }

    private NutritionTotals calculateNutrition(List<Map<String, String>> ingredients) {
        logger.debug("Calculating nutrition for ingredients: {}", ingredients);
        double totalFat = 0.0, totalSodium = 0.0, totalPotassium = 0.0,
                totalCholestrol = 0.0, totalCarbohydrates = 0.0,
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
                logger.debug("Found food item in local DB: {}", name);
            } else {
                logger.debug("Fetching food item from external API: {}", name);
                // Build better query for external API
                String apiQuery = buildApiQuery(name, quantityStr);
                logger.debug("External API query: {}", apiQuery);

                food = externalApiService.fetchNutritionInfo(apiQuery)
                        .map(foodRepo::save)
                        .orElse(null);
                isFromExternalApi = (food != null);
            }

            if (food != null) {
                if (isFromExternalApi) {
                    totalFat += safeNullableDouble(food.getTotalFat());
                    totalSodium += safeNullableDouble(food.getSodium());
                    totalPotassium += safeNullableDouble(food.getPotassium());
                    totalCholestrol += safeNullableDouble(food.getCholestrol());
                    totalCarbohydrates += safeNullableDouble(food.getCarbohydrates());
                    totalFiber += safeNullableDouble(food.getFiber());
                    totalSugar += safeNullableDouble(food.getSugar());
                } else {
                    totalFat += safeNullableDouble(food.getTotalFat()) * qty;
                    totalSodium += safeNullableDouble(food.getSodium()) * qty;
                    totalPotassium += safeNullableDouble(food.getPotassium()) * qty;
                    totalCholestrol += safeNullableDouble(food.getCholestrol()) * qty;
                    totalCarbohydrates += safeNullableDouble(food.getCarbohydrates()) * qty;
                    totalFiber += safeNullableDouble(food.getFiber()) * qty;
                    totalSugar += safeNullableDouble(food.getSugar()) * qty;
                }
            } else {
                logger.warn("Nutrition data not found for ingredient: {}", name);
                totalFat += 2 * qty;
                totalSodium += 150 * qty;
                totalPotassium += 100 * qty;
                totalCholestrol += 30 * qty;
                totalCarbohydrates += 10 * qty;
                totalFiber += 1 * qty;
                totalSugar += 5 * qty;
            }
        }
        return new NutritionTotals(totalFat, totalSodium, totalPotassium, totalCholestrol, totalCarbohydrates, totalFiber, totalSugar);
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

    public List<NutritionLog> getNutritionLogsByUserId(Long userId) {
        return logRepo.findByUserId(userId);
    }

    public List<NutritionLog> getNutritionLogsByMealType(Long userId, MealType mealType) {
        return logRepo.findByUserIdAndMealType(userId, mealType);
    }

    public DailyIntakeSummary getTodayIntakeSummary(Long userId, LocalDate date) {
        logger.debug("Calculating intake summary for user ID: {} on date: {}", userId, date);
        List<NutritionLog> todayLogs = logRepo.findByUserIdAndAnalyzedAt(userId, date);
        double totalFat = todayLogs.stream().mapToDouble(NutritionLog::getTotalFat).sum();
        double totalSodium = todayLogs.stream().mapToDouble(NutritionLog::getTotalSodium).sum();
        double totalPotassium = todayLogs.stream().mapToDouble(NutritionLog::getTotalPotassium).sum();
        double totalCholestrol = todayLogs.stream().mapToDouble(NutritionLog::getTotalCholestrol).sum();
        double totalCarbohydrates = todayLogs.stream().mapToDouble(NutritionLog::getTotalCarbohydrates).sum();
        double totalFiber = todayLogs.stream().mapToDouble(NutritionLog::getTotalFiber).sum();
        double totalSugar = todayLogs.stream().mapToDouble(NutritionLog::getTotalSugar).sum();
        return new DailyIntakeSummary(totalFat, totalSodium, totalPotassium, totalCholestrol, totalCarbohydrates, totalFiber, totalSugar);
    }

    public NutritionResponse updateNutritionLog(Long logId, NutritionRequest request, Long userId) {
        NutritionLog log = logRepo.findById(logId)
                .orElseThrow(() -> {
                    logger.warn("Nutrition log not found for ID: {}", logId);
                    return new RuntimeException("Nutrition log not found");
                });

        // Add authorization check
        if (!log.getUserId().equals(userId)) {
            logger.error("User ID: {} not authorized to update log ID: {}", userId, logId);
            throw new RuntimeException("User not authorized to update this log");
        }

        List<Map<String, String>> ingredients = request.getIngredients() != null ? request.getIngredients() : Collections.emptyList();
        NutritionTotals totals = calculateNutrition(ingredients);

        log.setRecipeId(request.getRecipeId());
        log.setFoodName(request.getRecipeName());
        log.setIngredients(String.join(", ", ingredients.stream().map(i -> i.get("name")).toList()));
        log.setTotalFat(totals.totalFat());
        log.setTotalSodium(totals.sodium());
        log.setTotalPotassium(totals.potassium());
        log.setTotalCholestrol(totals.cholestrol());
        log.setTotalCarbohydrates(totals.carbohydrates());
        log.setTotalFiber(totals.fiber());
        log.setTotalSugar(totals.sugar());
        log.setMealType(request.getMealType());
        log.setAnalyzedAt(LocalDate.now());

        logRepo.save(log);
        logger.info("Nutrition log ID: {} updated successfully by user ID: {}", logId, userId);

        return new NutritionResponse(
                log.getUserId(),
                log.getRecipeId(),
                log.getFoodName(),
                log.getTotalFat(),
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

    public NutritionLog patchNutritionLog(Long logId, Map<String, Object> updates, Long userId) {
        NutritionLog log = logRepo.findById(logId)
                .orElseThrow(() -> {
                    logger.warn("Nutrition log not found for ID: {}", logId);
                    return new RuntimeException("Nutrition log not found");
                });

        // Add authorization check
        if (!log.getUserId().equals(userId)) {
            logger.error("User ID: {} not authorized to patch log ID: {}", userId, logId);
            throw new RuntimeException("User not authorized to patch this log");
        }

        if (updates.containsKey("mealType")) {
            log.setMealType(MealType.valueOf((String) updates.get("mealType")));
            logger.debug("Patched mealType for log ID: {} to {}", logId, updates.get("mealType"));
        }else{
            logger.debug("No mealType update provided for log ID: {}", logId);
        }

        return logRepo.save(log);
    }

    public void deleteNutritionLog(Long logId, long userId) {
        NutritionLog log = logRepo.findById(logId)
                .orElseThrow(() -> {
                    logger.warn("Nutrition log not found for ID: {}", logId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutrition log not found with id: " + logId);
                });

        if (!log.getUserId().equals(userId)) {
            logger.error("User ID: {} not authorized to delete log ID: {}", userId, logId);
            throw new RuntimeException("User not authorized to delete this log");
        }

        logRepo.delete(log);
        logger.info("Nutrition log ID: {} deleted successfully by user ID: {}", logId, userId);
    }

    private double safeNullableDouble(Double v) {
        return v == null ? 0.0 : v;
    }
}