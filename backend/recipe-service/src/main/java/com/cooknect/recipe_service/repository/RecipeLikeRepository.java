package com.cooknect.recipe_service.repository;

import com.cooknect.recipe_service.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipeLikeRepository extends JpaRepository<Like,Long> {
//    return like list by recipe id
    Optional<Like> getByRecipeIdAndUserId(Long recipeId, Long userId);
}
