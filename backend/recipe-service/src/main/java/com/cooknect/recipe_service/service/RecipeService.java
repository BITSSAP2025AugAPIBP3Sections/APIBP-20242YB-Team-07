package com.cooknect.recipe_service.service;

import com.cooknect.recipe_service.dto.GetCommentDto;
import com.cooknect.recipe_service.dto.GetRecipeDTO;
import com.cooknect.recipe_service.dto.RecipeCreateDTO;
import com.cooknect.recipe_service.exception.ForbiddenException;
import com.cooknect.recipe_service.model.*;
import com.cooknect.recipe_service.repository.RecipeLikeRepository;
import com.cooknect.recipe_service.repository.RecipeRepository;
import com.cooknect.recipe_service.exception.NotFoundException;
import com.cooknect.recipe_service.repository.RecipeSavedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class RecipeService {

    private final RecipeRepository repo;
    private final RecipeLikeRepository likeRepository;
    private final RecipeSavedRepository savedRepository;

    @Autowired
    private RestTemplate restTemplate;
    @Value("${user.service.url}")
    private String userBaseUrl;


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
        Recipe recipe = getRecipeById(recipeId);
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

    /* Save or Unsave a Recipe */
    public void saveAndUnsave(Long recipeId, Long userId) {
        Recipe recipe = getRecipeById(recipeId);
        if(recipe==null){
            throw new NotFoundException("Recipe not found: " + recipeId);
        }
        var existingSaved = savedRepository.getByRecipeIdAndUserId(recipeId, userId);
        if (existingSaved.isPresent()) {
            /* Unsave a Recipe */
            savedRepository.delete(existingSaved.get());
        }
        else{
            /* Save a Recipe */
            SavedRecipe saved = new SavedRecipe();
            saved.setUserId(userId);
            saved.setRecipeId(recipeId);
            savedRepository.save(saved);
        }

    }

    /* Adding comment to a Recipe */
    public void addComment(Long recipeId, Comment comment) {
        Recipe recipe = getRecipeById(recipeId);
        if(recipe==null){
            throw new NotFoundException("Recipe not found: " + recipeId);
        }

        recipe.getComments().add(comment);

        // Persist the comment + updated recipe
        repo.save(recipe);
    }

    /* Get all recipes */
    public List<GetRecipeDTO> getAllRecipes(Long userId) {
        List<Recipe> recipes = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));

        /*
         * Collect unique userIds.
         * Doing this so that for all recipes username is fetched at once.
         * Which avoid repeated calls to user service.
        */
        List<Long> userIds = recipes.stream()
                .map(Recipe::getUserId)
                .distinct()
                .toList();
        // Build request body with all userIds
        HttpEntity<List<Long>> request = new HttpEntity<>(userIds);

        // Call the usernames endpoint
        ResponseEntity<Map<Long, String>> response = restTemplate.exchange(
                userBaseUrl + "/usernames",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<Long, String>>() {}
        );

        Map<Long, String> userIdToUsername = response.getBody();


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
            // Set username from user-service
            dto.setUsername(userIdToUsername.get(recipe.getUserId()));
            dto.setUserId(recipe.getUserId());
            dto.setCommentCount(recipe.getComments().size());
            return dto;
        }).toList();
    }

    /* Get all Recipes based on user Id */
    public List<GetRecipeDTO> getRecipesByUserId(Long userId, Long requesterId) {
        List<Recipe> recipes = repo.findByUserId(userId);

        return recipes.stream().map(recipe -> {
            GetRecipeDTO dto = new GetRecipeDTO();
            dto.setId(recipe.getId());
            dto.setTitle(recipe.getTitle());
            dto.setDescription(recipe.getDescription());
            dto.setCuisine(recipe.getCuisine().toString());
            dto.setRecipeImageUrl(recipe.getRecipeImageUrl());
            dto.setIngredients(recipe.getIngredients());
            dto.setPreparation(recipe.getPreparation());
            dto.setLikesCount(recipe.getLikes());
            dto.setLikedByUser(likeRepository.getByRecipeIdAndUserId(recipe.getId(), requesterId).isPresent());
            dto.setSavedByUser(savedRepository.getByRecipeIdAndUserId(recipe.getId(), requesterId).isPresent());
            dto.setCommentCount(recipe.getComments().size());

            dto.setComments(
                    recipe.getComments().stream().map(comment -> {
                        GetCommentDto c = new GetCommentDto();
                        c.setAuthor(comment.getAuthor());
                        c.setText(comment.getText());
                        return c;
                    }).toList()
            );

            return dto;
        }).toList();
    }
   /* Get Recipe by id based on User id */
    public GetRecipeDTO getById(Long recipeId, Long userId) {
        Recipe recipe = repo.findById(recipeId)
                .orElseThrow(() -> new NotFoundException("Recipe not found: " + recipeId));

        List<Long> userIds = List.of(recipe.getUserId());
        // Build request body with all userIds
        HttpEntity<List<Long>> request = new HttpEntity<>(userIds);
        // Call the usernames endpoint
        ResponseEntity<Map<Long, String>> response = restTemplate.exchange(
                userBaseUrl + "/usernames",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<Long, String>>() {}
        );
        Map<Long, String> userIdToUsername = response.getBody();

        GetRecipeDTO dto = new GetRecipeDTO();
        dto.setId(recipe.getId());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setCuisine(recipe.getCuisine().toString());
        dto.setRecipeImageUrl(recipe.getRecipeImageUrl());
        dto.setUserId(recipe.getUserId());
        dto.setComments(
                recipe.getComments().stream().map(comment -> {
                    GetCommentDto c = new GetCommentDto();
                    c.setAuthor(comment.getAuthor());
                    c.setText(comment.getText());
                    return c;
                }).toList()
        );

        dto.setIngredients(recipe.getIngredients());
        dto.setPreparation(recipe.getPreparation());
        dto.setLikesCount(recipe.getLikes());

        dto.setLikedByUser(
                likeRepository.getByRecipeIdAndUserId(recipe.getId(), userId).isPresent()
        );

        dto.setSavedByUser(
                savedRepository.getByRecipeIdAndUserId(recipe.getId(), userId).isPresent()
        );
        // Set username from user-service
        dto.setUsername(userIdToUsername.get(recipe.getUserId()));
        dto.setCommentCount(recipe.getComments().size());

        return dto;
    }

    /* Get Recipe by id */
    public Recipe getRecipeById(Long recipeId) {
        return repo.findById(recipeId)
                .orElseThrow(() -> new NotFoundException("Recipe not found: " + recipeId));
    }



    public List<Recipe> listAll() {
        return repo.findAll();
    }
    // PATCH — partial update
    public Recipe patchUpdate(Long id, Recipe updates, Long userId) {
        Recipe existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found: " + id));

        // Optional: Prevent unauthorized users from editing others’ recipes
        if (!existing.getUserId().equals(userId)) {
            throw new ForbiddenException("You cannot update the user of the recipe");
        }

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
    /* Delete a Recipe based on user Id */
    public void deleteRecipeByUser(Long userId, Long recipeId) {
        Recipe recipe = repo.findById(recipeId)
                .orElseThrow(() -> new NotFoundException("Recipe not found: " + recipeId));

        // Ensure the user is the owner
        if (!recipe.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to delete this recipe");
        }

        repo.delete(recipe);
    }

    public void deleteAllByUser(Long userId) {
        List<Recipe> recipes = repo.findAllByUserId((userId));
        repo.deleteAll(recipes);
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
