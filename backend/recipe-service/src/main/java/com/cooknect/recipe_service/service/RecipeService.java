package com.cooknect.recipe_service.service;

import com.cooknect.recipe_service.model.*;
import com.cooknect.recipe_service.repository.RecipeRepository;
import com.cooknect.recipe_service.integration.SpoonacularClient;
import com.cooknect.recipe_service.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository repo;
    private final SpoonacularClient spoonacularClient;

    // Constructor Injection for both dependencies
    public RecipeService(RecipeRepository repo, SpoonacularClient spoonacularClient) {
        this.repo = repo;
        this.spoonacularClient = spoonacularClient;
    }

    //  CRUD Operations

    public Recipe create(Recipe recipe) {
        return repo.save(recipe);
    }

    public Recipe getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found: " + id));
    }

    public List<Recipe> listAll() {
        return repo.findAll();
    }

    public Recipe update(Long id, Recipe update) {
        Recipe existing = getById(id);
        existing.setTitle(update.getTitle());
        existing.setDescription(update.getDescription());
        existing.setIngredients(update.getIngredients());
        existing.setCuisine(update.getCuisine());
        existing.setLanguage(update.getLanguage());
        return repo.save(existing);
    }

    // PATCH — partial update
    public Recipe patchUpdate(Long id, Recipe updates) {
        Recipe existing = getById(id);

        if (updates.getTitle() != null)
            existing.setTitle(updates.getTitle());
        if (updates.getDescription() != null)
            existing.setDescription(updates.getDescription());
        if (updates.getIngredients() != null && !updates.getIngredients().isEmpty())
            existing.setIngredients(updates.getIngredients());
        if (updates.getCuisine() != null)
            existing.setCuisine(updates.getCuisine());
        if (updates.getLanguage() != null)
            existing.setLanguage(updates.getLanguage());

        return repo.save(existing);
    }

    public void delete(Long id) {
        if (!repo.existsById(id))
            throw new NotFoundException("Recipe not found: " + id);
        repo.deleteById(id);
    }

    public Recipe like(Long id) {
        Recipe r = getById(id);
        r.setLikes(r.getLikes() + 1);
        return repo.save(r);
    }

    public Recipe addComment(Long id, Comment comment) {
        Recipe r = getById(id);
        r.getComments().add(comment);
        return repo.save(r);
    }

    public List<Recipe> searchByTitle(String q) {
        return repo.findByTitleContainingIgnoreCase(q);
    }

    public List<Recipe> findByCuisine(Cuisine cuisine) {
        return repo.findByCuisine(cuisine);
    }

    public List<Recipe> findByIngredient(String ingredient) {
        // Fetch all recipes and filter manually by ingredient name
        return repo.findAll().stream()
                .filter(r -> r.getIngredients() != null &&
                        r.getIngredients().stream()
                                .anyMatch(i -> i.getName() != null &&
                                        i.getName().toLowerCase().contains(ingredient.toLowerCase())))
                .toList();
    }

    //  Integration with Spoonacular
    public String fetchExternalRecipes(String query) {
        return spoonacularClient.searchRecipes(query);
    }
}
