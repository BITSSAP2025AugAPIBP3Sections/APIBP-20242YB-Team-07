package com.cooknect.recipe_service.service;

import com.cooknect.recipe_service.dto.GetCommentDto;
import com.cooknect.recipe_service.dto.GetRecipeDTO;
import com.cooknect.recipe_service.dto.RecipeCreateDTO;
import com.cooknect.common.dto.PageRequestDTO;
import com.cooknect.common.dto.PageResponseDTO;
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
    public PageResponseDTO<GetRecipeDTO> getAllRecipes(Long userId, PageRequestDTO pageRequestDTO) {
        String[] sortFields = pageRequestDTO.getSortBy().split(",");
        String[] directions = pageRequestDTO.getDirection().split(",");
        Sort sort = Sort.unsorted();
        for (int i = 0; i < sortFields.length; i++) {
            String field = sortFields[i].trim();
            Sort.Direction dir = Sort.Direction.ASC;
            if (i < directions.length) {
                dir = Sort.Direction.fromString(directions[i].trim());
            }
            sort = sort.and(Sort.by(dir, field));
        }
        List<Recipe> recipes = repo.findAll(sort);

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
        Map<Long, String> userIdToUsername = null;
        try {
            ResponseEntity<Map<Long, String>> response = restTemplate.exchange(
                    userBaseUrl + "/usernames",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<Long, String>>() {}
            );
            userIdToUsername = response.getBody();
        } catch (Exception e) {
            // If user service is not available or configured, continue without usernames
            System.out.println("Warning: Could not fetch usernames from user service: " + e.getMessage());
        }
        
        final Map<Long, String> finalUserIdToUsername = userIdToUsername;

        List<GetRecipeDTO> recipeDTOs = recipes.stream().map(recipe -> {
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
            if (finalUserIdToUsername != null) {
                dto.setUsername(finalUserIdToUsername.get(recipe.getUserId()));
            }
            dto.setUserId(recipe.getUserId());
            dto.setCommentCount(recipe.getComments().size());
            return dto;
        }).toList();

        // Apply pagination
        int page = pageRequestDTO.getPage();
        int size = pageRequestDTO.getSize();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, recipeDTOs.size());
        
        List<GetRecipeDTO> paginatedRecipes = startIndex >= recipeDTOs.size() ? 
            List.of() : recipeDTOs.subList(startIndex, endIndex);

        // Create PageResponseDTO
        PageResponseDTO<GetRecipeDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(paginatedRecipes);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(recipeDTOs.size());
        pageResponse.setTotalPages((int) Math.ceil((double) recipeDTOs.size() / size));
        pageResponse.setSort(pageRequestDTO.getSortBy() + "," + pageRequestDTO.getDirection());
        
        return pageResponse;
    }

    /* Get all Recipes based on title search */
    public PageResponseDTO<GetRecipeDTO> getRecipesByTitle(String title, Long userId, Boolean saved, PageRequestDTO pageRequestDTO) {
        List<Recipe> recipes = repo.findByTitleContainingIgnoreCase(title);

        if(recipes.isEmpty()){
            PageResponseDTO<GetRecipeDTO> emptyResponse = new PageResponseDTO<>();
            emptyResponse.setContent(List.of());
            emptyResponse.setPage(0);
            emptyResponse.setSize(0);
            emptyResponse.setTotalElements(0);
            emptyResponse.setTotalPages(0);
            return emptyResponse;
        }

//        if saved is true, filter this reveived recipes from the indByTitleContainingIgnoreCase to only those saved by the user
        if(saved != null && saved){
            List<Recipe> savedRecipes = recipes.stream()
                    .filter(recipe -> savedRepository.getByRecipeIdAndUserId(recipe.getId(), userId).isPresent())
                    .toList();
            recipes = savedRecipes;
            if(recipes.isEmpty()) {
                PageResponseDTO<GetRecipeDTO> emptyResponse = new PageResponseDTO<>();
                emptyResponse.setContent(List.of());
                emptyResponse.setPage(0);
                emptyResponse.setSize(0);
                emptyResponse.setTotalElements(0);
                emptyResponse.setTotalPages(0);
                return emptyResponse;
            }
        }


        List<Long> userIds = recipes.stream()
                .map(Recipe::getUserId)
                .distinct()
                .toList();
        HttpEntity<List<Long>> request = new HttpEntity<>(userIds);

        Map<Long, String> userIdToUsername = null;
        try {
            ResponseEntity<Map<Long, String>> response = restTemplate.exchange(
                    userBaseUrl + "/usernames",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<Long, String>>() {}
            );
            userIdToUsername = response.getBody();
        } catch (Exception e) {
            System.out.println("Warning: Could not fetch usernames from user service: " + e.getMessage());
        }

        final Map<Long, String> finalUserIdToUsername = userIdToUsername;

        List<GetRecipeDTO> recipeDTOs = recipes.stream().map(recipe -> {
            GetRecipeDTO dto = new GetRecipeDTO();
            dto.setId(recipe.getId());
            dto.setTitle(recipe.getTitle());
            dto.setDescription(recipe.getDescription());
            dto.setCuisine(recipe.getCuisine().toString());
            dto.setRecipeImageUrl(recipe.getRecipeImageUrl());
            dto.setIngredients(recipe.getIngredients());
            dto.setPreparation(recipe.getPreparation());
            dto.setLikesCount(recipe.getLikes());
            dto.setLikedByUser(likeRepository.getByRecipeIdAndUserId(recipe.getId(), userId).isPresent());
            dto.setSavedByUser(savedRepository.getByRecipeIdAndUserId(recipe.getId(), userId).isPresent());
            dto.setCommentCount(recipe.getComments().size());
            dto.setUserId(recipe.getUserId());
            if (finalUserIdToUsername != null) {
                dto.setUsername(finalUserIdToUsername.get(recipe.getUserId()));
            }
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

        int page = pageRequestDTO.getPage();
        int size = pageRequestDTO.getSize();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, recipeDTOs.size());

        List<GetRecipeDTO> paginatedRecipes = startIndex >= recipeDTOs.size() ?
                List.of() : recipeDTOs.subList(startIndex, endIndex);

        PageResponseDTO<GetRecipeDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(paginatedRecipes);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(recipeDTOs.size());
        pageResponse.setTotalPages((int) Math.ceil((double) recipeDTOs.size() / size));
        pageResponse.setSort(pageRequestDTO.getSortBy() + "," + pageRequestDTO.getDirection());

        return pageResponse;
    }


    /* Get all Recipes based on user Id */
    public PageResponseDTO<GetRecipeDTO> getRecipesByUserId(Long userId, PageRequestDTO pageRequestDTO) {
        List<Recipe> recipes = repo.findByUserId(userId);

        if(recipes.isEmpty()){
            PageResponseDTO<GetRecipeDTO> emptyResponse = new PageResponseDTO<>();
            emptyResponse.setContent(List.of());
            emptyResponse.setPage(0);
            emptyResponse.setSize(0);
            emptyResponse.setTotalElements(0);
            emptyResponse.setTotalPages(0);
            return emptyResponse;
        }

        List<Long> userIds = List.of(userId);
        // Build request body with all userIds
        HttpEntity<List<Long>> request = new HttpEntity<>(userIds);
        
        // Call the usernames endpoint
        Map<Long, String> userIdToUsername = null;
        try {
            ResponseEntity<Map<Long, String>> response = restTemplate.exchange(
                    userBaseUrl + "/usernames",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<Long, String>>() {}
            );
            userIdToUsername = response.getBody();
        } catch (Exception e) {
            // If user service is not available or configured, continue without usernames
            System.out.println("Warning: Could not fetch usernames from user service: " + e.getMessage());
        }
        
        final Map<Long, String> finalUserIdToUsername = userIdToUsername;

        List<GetRecipeDTO> recipeDTOs = recipes.stream().map(recipe -> {
            GetRecipeDTO dto = new GetRecipeDTO();
            dto.setId(recipe.getId());
            dto.setTitle(recipe.getTitle());
            dto.setDescription(recipe.getDescription());
            dto.setCuisine(recipe.getCuisine().toString());
            dto.setRecipeImageUrl(recipe.getRecipeImageUrl());
            dto.setIngredients(recipe.getIngredients());
            dto.setPreparation(recipe.getPreparation());
            dto.setLikesCount(recipe.getLikes());
            dto.setLikedByUser(likeRepository.getByRecipeIdAndUserId(recipe.getId(), userId).isPresent());
            dto.setSavedByUser(savedRepository.getByRecipeIdAndUserId(recipe.getId(), userId).isPresent());
            dto.setCommentCount(recipe.getComments().size());
            dto.setUserId(recipe.getUserId());
            if (finalUserIdToUsername != null) {
                dto.setUsername(finalUserIdToUsername.get(recipe.getUserId()));
            }
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

        // Apply pagination
        int page = pageRequestDTO.getPage();
        int size = pageRequestDTO.getSize();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, recipeDTOs.size());
        
        List<GetRecipeDTO> paginatedRecipes = startIndex >= recipeDTOs.size() ? 
            List.of() : recipeDTOs.subList(startIndex, endIndex);

        // Create PageResponseDTO
        PageResponseDTO<GetRecipeDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(paginatedRecipes);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(recipeDTOs.size());
        pageResponse.setTotalPages((int) Math.ceil((double) recipeDTOs.size() / size));
        pageResponse.setSort(pageRequestDTO.getSortBy() + "," + pageRequestDTO.getDirection());
        
        return pageResponse;
    }

    /* Get all Recipes saved by User */
    public PageResponseDTO<GetRecipeDTO> getSavedRecipesByUserId(Long userId, PageRequestDTO pageRequestDTO) {
        List<SavedRecipe> savedRecipes = savedRepository.findByUserId(userId);
        if(savedRecipes.isEmpty()){
            PageResponseDTO<GetRecipeDTO> emptyResponse = new PageResponseDTO<>();
            emptyResponse.setContent(List.of());
            emptyResponse.setPage(0);
            emptyResponse.setSize(0);
            emptyResponse.setTotalElements(0);
            emptyResponse.setTotalPages(0);
            return emptyResponse;
        }
        List<Long> recipeIds = savedRecipes.stream()
                .map(SavedRecipe::getRecipeId)
                .toList();
        List<Recipe> recipes = repo.findAllById(recipeIds);

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

        List<GetRecipeDTO> recipeDTOs = recipes.stream().map(recipe -> {
            GetRecipeDTO dto = new GetRecipeDTO();
            dto.setId(recipe.getId());
            dto.setTitle(recipe.getTitle());
            dto.setDescription(recipe.getDescription());
            dto.setCuisine(recipe.getCuisine().toString());
            dto.setRecipeImageUrl(recipe.getRecipeImageUrl());
            dto.setIngredients(recipe.getIngredients());
            dto.setPreparation(recipe.getPreparation());
            dto.setLikesCount(recipe.getLikes());
            dto.setLikedByUser(likeRepository.getByRecipeIdAndUserId(recipe.getId(), userId).isPresent());
            dto.setSavedByUser(true); // Since these are saved recipes
            dto.setCommentCount(recipe.getComments().size());
            dto.setUserId(recipe.getUserId());
            if (userIdToUsername != null) {
                dto.setUsername(userIdToUsername.get(recipe.getUserId()));
            }
            return dto;
        }).toList();

        // Apply pagination
        int page = pageRequestDTO.getPage();
        int size = pageRequestDTO.getSize();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, recipeDTOs.size());
        
        List<GetRecipeDTO> paginatedRecipes = startIndex >= recipeDTOs.size() ? 
            List.of() : recipeDTOs.subList(startIndex, endIndex);

        // Create PageResponseDTO
        PageResponseDTO<GetRecipeDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(paginatedRecipes);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(recipeDTOs.size());
        pageResponse.setTotalPages((int) Math.ceil((double) recipeDTOs.size() / size));
        pageResponse.setSort(pageRequestDTO.getSortBy() + "," + pageRequestDTO.getDirection());
        
        return pageResponse;
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
        List<Recipe> allMatches = repo.findByTitleContainingIgnoreCase(q);
        
        if (allMatches.isEmpty()) {
            return allMatches;
        }
        
        // Find the recipe with the maximum number of likes
        Recipe mostLikedRecipe = allMatches.stream()
            .max((r1, r2) -> Integer.compare(r1.getLikes(), r2.getLikes()))
            .orElse(allMatches.get(0));
        
        return List.of(mostLikedRecipe);
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

    public Map<String, Object> getUserDetailsById(Long userId) {
        String userServiceUrl = userBaseUrl + userId;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> user = response.getBody();
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

}
