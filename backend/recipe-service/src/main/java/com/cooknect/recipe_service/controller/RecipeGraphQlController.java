package com.cooknect.recipe_service.controller;

import com.cooknect.recipe_service.model.*;
import com.cooknect.recipe_service.service.RecipeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RecipeGraphQlController {

    private final RecipeService svc;

    public RecipeGraphQlController(RecipeService svc) {
        this.svc = svc;
    }

    // === QUERIES ===

    @QueryMapping(name = "recipes")
    public List<Recipe> recipes() {
        return svc.listAll();
    }

    @QueryMapping(name = "recipeById")
    public Recipe recipeById(@Argument Long id) {
        return svc.getById(id);
    }

    @QueryMapping(name = "searchRecipes")
    public List<Recipe> searchRecipes(@Argument String title) {
        return svc.searchByTitle(title);
    }

    @QueryMapping(name = "recipesByCuisine")
    public List<Recipe> recipesByCuisine(@Argument String cuisine) {
        Cuisine c;
        try {
            c = Cuisine.valueOf(cuisine.toUpperCase());
        } catch (Exception e) {
            c = Cuisine.OTHER;
        }
        return svc.findByCuisine(c);
    }

    @QueryMapping(name = "recipesByIngredient")
    public List<Recipe> recipesByIngredient(@Argument String name) {
        return svc.findByIngredient(name);
    }

//    @QueryMapping(name = "externalRecipes")
//    public List<ExternalRecipe> externalRecipes(@Argument String query) {
//        return svc.fetchExternalRecipes(query);
//    }

    // === MUTATIONS ===

//    @MutationMapping(name = "addRecipe")
//    public Recipe addRecipe(
//            @Argument String title,
//            @Argument String description,
//            @Argument Cuisine cuisine,
//            @Argument List<Ingredient> ingredients,
//            @Argument String username
//
//    ) {
//        Recipe recipe = new Recipe();
//        recipe.setTitle(title);
//        recipe.setDescription(description);
//        recipe.setCuisine(cuisine);
//        recipe.setUsername(username);
//        recipe.setIngredients(ingredients);
//        return svc.create(recipe);
//
//    }

    @MutationMapping(name = "updateRecipe")
    public Recipe updateRecipe(
            @Argument Long id,
            @Argument String title,
            @Argument String description,
            @Argument Cuisine cuisine
    ) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setCuisine(cuisine);
        return svc.update(id, recipe);
    }

    @MutationMapping(name = "deleteRecipe")
    public Boolean deleteRecipe(@Argument Long id) {
        svc.delete(id);
        return true;
    }

//    @MutationMapping(name = "likeRecipe")
//    public Recipe likeRecipe(@Argument Long id) {
//        return svc.like(id);
//    }


    @MutationMapping(name = "likeRecipe")
    public Boolean likeRecipe(
            @Argument Long id,
            HttpServletRequest request
    ) {
        String userIdHeader = request.getHeader("X-User-Id");

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid X-User-Id header");
        }

        svc.likeAndUnlike(id, userId);
        return true;
    }


    @MutationMapping(name = "addComment")
    public Boolean addComment(
            @Argument Long recipeId,
            @Argument String text,
            HttpServletRequest request
    ) {
        String userIdHeader = request.getHeader("X-User-Id");

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid X-User-Id header");
        }

        Comment comment = new Comment();
        comment.setAuthor(String.valueOf(userId));
        comment.setText(text);

        svc.addComment(recipeId, comment);
        return true;
    }

}
