package com.cooknect.recipe_service.repository;

import com.cooknect.recipe_service.model.Recipe;
import com.cooknect.recipe_service.model.Cuisine;
import com.cooknect.recipe_service.model.Ingredient;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByCuisine(Cuisine cuisine);
    // Custom query for ingredient name search
    @Query("SELECT r FROM Recipe r JOIN r.ingredients i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Recipe> findByIngredientName(@Param("name") String name);
    List<Recipe> findByTitleContainingIgnoreCase(String title);
}