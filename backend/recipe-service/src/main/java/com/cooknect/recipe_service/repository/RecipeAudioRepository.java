package com.cooknect.recipe_service.repository;

import com.cooknect.recipe_service.model.RecipeAudio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecipeAudioRepository extends JpaRepository<RecipeAudio, Long> {
    Optional<RecipeAudio> findByRecipeId(Long recipeId);
}