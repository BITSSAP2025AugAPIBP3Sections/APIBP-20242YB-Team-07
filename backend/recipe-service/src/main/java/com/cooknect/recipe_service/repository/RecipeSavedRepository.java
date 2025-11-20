package com.cooknect.recipe_service.repository;

import com.cooknect.recipe_service.model.SavedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipeSavedRepository extends JpaRepository<SavedRecipe, Long> {
    Optional<SavedRecipe> getByRecipeIdAndUserId(Long recipeId, Long userId);
    List<SavedRecipe> findByUserId(Long userId);
}
