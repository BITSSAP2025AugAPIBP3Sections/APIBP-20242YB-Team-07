package com.cooknect.recipe_service.repository;

import com.cooknect.recipe_service.model.RecipeAudio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecipeAudioRepository extends JpaRepository<RecipeAudio, Long> {
    Optional<RecipeAudio> findByRecipeId(Long recipeId);
    List<RecipeAudio> findAllByRecipeId(Long recipeId);
    void deleteAllByRecipeId(Long recipeId);
    Optional<RecipeAudio> findByRecipeIdAndLanguage(Long recipeId, String language);
}