package com.cooknect.recipe_service.service;

import com.cooknect.recipe_service.dto.GetCommentDto;
import com.cooknect.recipe_service.dto.GetRecipeDTO;
import com.cooknect.recipe_service.dto.RecipeCreateDTO;
import com.cooknect.recipe_service.model.*;
import com.cooknect.recipe_service.repository.RecipeLikeRepository;
import com.cooknect.recipe_service.repository.RecipeRepository;
import com.cooknect.recipe_service.exception.NotFoundException;
import com.cooknect.recipe_service.repository.RecipeSavedRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository repo;
    private final RecipeLikeRepository likeRepository;
    private final RecipeSavedRepository savedRepository;

    /* Constructor Injection for both dependencies */
    public RecipeService(RecipeRepository repo, RecipeLikeRepository likeRepository, RecipeSavedRepository savedRepository) {
        this.likeRepository = likeRepository;
        this.savedRepository = savedRepository;
        this.repo = repo;
    }

    /* Creating a new recipe */
    public Recipe create(RecipeCreateDTO recipe, Long userId) {
        Recipe newRecipe = new Recipe();
        newRecipe.setTitle(recipe.getTitle());
        newRecipe.setDescription(recipe.getDescription());
        newRecipe.setIngredients(recipe.getIngredients());
        newRecipe.setPreparation(recipe.getPreparation());
        if (recipe.getCuisine() != null) {
            try {
                Cuisine cuisine = Cuisine.valueOf(recipe.getCuisine().toUpperCase());
                newRecipe.setCuisine(cuisine);
            } catch (IllegalArgumentException e) {
                newRecipe.setCuisine(Cuisine.OTHER);
            }
        } else {
            newRecipe.setCuisine(Cuisine.OTHER);
        }
        newRecipe.setUserId(userId);
        newRecipe.setRecipeImageUrl(recipe.getRecipeImageUrl());
        return repo.save(newRecipe);
    }

    /* Like or Unlike a recipe */
    public void likeAndUnlike(Long recipeId, Long userId) {
        Recipe recipe = getById(recipeId);
        if(recipe==null){
            throw new NotFoundException("Recipe not found: " + recipeId);
        }
        var existingLike = likeRepository.getByRecipeIdAndUserId(recipeId, userId);
        if (existingLike.isPresent()) {
            /* Unlike Recipe */
            likeRepository.delete(existingLike.get());
            recipe.setLikes(recipe.getLikes() - 1);
        } else {
            /* Like Recipe */
            Like newLike = new Like();
            newLike.setUserId(userId);
            newLike.setRecipeId(recipeId);
            likeRepository.save(newLike);
            recipe.setLikes(recipe.getLikes() + 1);
        }
        repo.save(recipe);
    }

    /* Adding comment to a Recipe */
    public void addComment(Long recipeId, Comment comment) {
        Recipe recipe = getById(recipeId);
        if(recipe==null){
            throw new NotFoundException("Recipe not found: " + recipeId);
        }

        recipe.getComments().add(comment);

        // Persist the comment + updated recipe
        repo.save(recipe);
    }

    /* Get all recipes */
    public List<GetRecipeDTO> getAllRecipes(Long userId) {
        List<Recipe> recipes = repo.findAll();

        /*
         * Collect unique userIds.
         * Doing this so that for all recipes username is fetched at once.
         * Which avoid repeated calls to user service.
        */
        List<Long> userIds = recipes.stream()
                .map(Recipe::getUserId)
                .distinct()
                .toList();

        return recipes.stream().map(recipe -> {
            GetRecipeDTO dto = new GetRecipeDTO();
            dto.setId(recipe.getId());
            dto.setTitle(recipe.getTitle());
            dto.setDescription(recipe.getDescription());
            dto.setCuisine(recipe.getCuisine().toString());
            dto.setRecipeImageUrl(recipe.getRecipeImageUrl());
            dto.setComments(
                    recipe.getComments().stream().map(comment -> {
                        GetCommentDto commentDto = new GetCommentDto();
                        commentDto.setAuthor(comment.getAuthor());
                        commentDto.setText(comment.getText());
                        return commentDto;
                    }).toList()
            );
            dto.setIngredients(recipe.getIngredients());
            dto.setPreparation(recipe.getPreparation());
            dto.setLikesCount(recipe.getLikes());
            dto.setLikedByUser(likeRepository.getByRecipeIdAndUserId(recipe.getId(), userId).isPresent());
            dto.setSavedByUser(savedRepository.getByRecipeIdAndUserId(recipe.getId(), userId).isPresent());
//            dto.setUsername();
            dto.setCommentCount(recipe.getComments().size());
            return dto;
        }).toList();
    }


    public List<Recipe> listAll() {
        return repo.findAll();
    }

    public Recipe getById(Long recipeId) {
        return repo.findById(recipeId)
                .orElseThrow(() -> new NotFoundException("Recipe not found: " + recipeId));
    }

    //Fetching all the recipes based on username
    public List<Recipe> getByUsername(String username) {
        return repo.findByUsername(username);
    }
    //Getting a particular recipe of a particular user
    // Fetch a single recipe by username and ID
    public Recipe getByUsernameAndId(String username, Long recipeId) {
        return repo.findByUsernameAndId(username, recipeId)
                .orElseThrow(() -> new RuntimeException(
                        "Recipe not found for user: " + username + " and id: " + recipeId));
    }
    //Deleting a recipe of a user
    public void deleteRecipeByUser(String username, Long recipeId) {
        Recipe recipe = repo.findById(recipeId)
                .orElseThrow(() -> new NotFoundException("Recipe not found with id: " + recipeId));

        // ensure the recipe belongs to the user
//        if (!recipe.getUsername().equals(username)) {
//            throw new RuntimeException("User not authorized to delete this recipe");
//        }

        repo.delete(recipe);
    }

    //Deleting all the recipes of a user

    public void deleteAllByUser(String username) {
        List<Recipe> userRecipes = repo.findByUsername(username);
        if (userRecipes.isEmpty()) {
            throw new NotFoundException("No recipes found for user: " + username);
        }

        repo.deleteAll(userRecipes);
    }

    //update username and assign all the recipes to that username
    public void updateUsername(String oldUsername, String newUsername) {
        List<Recipe> recipes = repo.findByUsername(oldUsername);

        if (recipes.isEmpty()) {
            throw new NotFoundException("No recipes found for user: " + oldUsername);
        }

//        for (Recipe recipe : recipes) {
//            recipe.setUsername(newUsername);
//        }

        repo.saveAll(recipes);
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



}
