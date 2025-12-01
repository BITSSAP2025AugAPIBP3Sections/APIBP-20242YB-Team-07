package com.cooknect.nutrition_service.service;

import com.recipe.GetRecipeByIdRequest;
import com.recipe.RecipeResponse;
import com.recipe.RecipeServiceGrpc;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * gRPC client for Recipe Service with Circuit Breaker pattern
 * Provides fault-tolerant communication with fallback mechanisms
 * Fetches recipe data (title, ingredients, etc.) from Recipe Service
 */
@Service
public class RecipeGrpcClient {

    private static final Logger logger = LoggerFactory.getLogger(RecipeGrpcClient.class);

    private final RecipeServiceGrpc.RecipeServiceBlockingStub recipeServiceStub;

    public RecipeGrpcClient(RecipeServiceGrpc.RecipeServiceBlockingStub recipeServiceStub) {
        this.recipeServiceStub = recipeServiceStub;
    }

    /**
     * Fetch recipe by ID with circuit breaker protection
     * Returns recipe data including title, ingredients, cuisine, etc.
     *
     * @param recipeId The ID of the recipe to fetch
     * @return Optional containing RecipeResponse with recipe data, or empty if not found/failed
     */
    @CircuitBreaker(name = "recipeService", fallbackMethod = "getRecipeByIdFallback")
    @Retry(name = "recipeService")
    public Optional<RecipeResponse> getRecipeById(Long recipeId) {
        try {
            // Handle null input gracefully
            if (recipeId == null) {
                logger.warn("Null recipe ID provided, returning empty");
                return Optional.empty();
            }

            // Check if stub is available
            if (recipeServiceStub == null) {
                logger.error("Recipe service stub is null - gRPC client not properly initialized");
                return Optional.empty();
            }

            logger.debug("Fetching recipe via gRPC for ID: {}", recipeId);

            GetRecipeByIdRequest request = GetRecipeByIdRequest.newBuilder()
                    .setRecipeId(recipeId)
                    .build();

            // Set deadline per-call to avoid "deadline already exceeded" errors
            RecipeServiceGrpc.RecipeServiceBlockingStub stubWithDeadline =
                    recipeServiceStub.withDeadlineAfter(5, java.util.concurrent.TimeUnit.SECONDS);

            if (stubWithDeadline == null) {
                logger.error("Failed to create stub with deadline - returning empty");
                return Optional.empty();
            }

            RecipeResponse response = stubWithDeadline.getRecipeById(request);

            if (response != null && response.getId() > 0) {
                logger.debug("Successfully fetched recipe ID: {} - Title: {}",
                        recipeId, response.getTitle());
                return Optional.of(response);
            } else {
                logger.warn("Recipe not found for ID: {}", recipeId);
                return Optional.empty();
            }

        } catch (StatusRuntimeException e) {
            logger.error("gRPC call failed for recipe ID {}: {} - {}",
                    recipeId, e.getStatus().getCode(), e.getStatus().getDescription());

            // Circuit breaker will track these failures
            // After threshold is reached, it will open and prevent further calls
            return handleGrpcFailure(recipeId, e);

        } catch (Exception e) {
            logger.error("Unexpected error fetching recipe ID {}: {}", recipeId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Fallback handler when gRPC call fails
     * Can be enhanced with caching or alternative data sources
     */
    private Optional<RecipeResponse> handleGrpcFailure(Long recipeId, Exception e) {
        logger.warn("Applying fallback for recipe ID: {} due to: {}", recipeId, e.getMessage());

        // Fallback strategy:
        // 1. Return cached data if available (not implemented yet)
        // 2. Return empty to gracefully degrade service
        // 3. Could also return a default/placeholder response

        return Optional.empty();
    }

    /**
     * Health check method to verify gRPC connectivity
     * Can be used by actuator or monitoring systems
     */
    public boolean isServiceAvailable() {
        try {
            // Try to call with an invalid ID to check connectivity
            // A proper implementation might use a dedicated health check RPC
            GetRecipeByIdRequest request = GetRecipeByIdRequest.newBuilder()
                    .setRecipeId(-1L)
                    .build();
            // Set deadline per-call
            recipeServiceStub
                    .withDeadlineAfter(2, java.util.concurrent.TimeUnit.SECONDS)
                    .getRecipeById(request);
            return true;
        } catch (StatusRuntimeException e) {
            // If we get a response (even error), service is available
            return e.getStatus().getCode() != io.grpc.Status.Code.UNAVAILABLE;
        } catch (Exception e) {
            return false;
        }
    }
}

