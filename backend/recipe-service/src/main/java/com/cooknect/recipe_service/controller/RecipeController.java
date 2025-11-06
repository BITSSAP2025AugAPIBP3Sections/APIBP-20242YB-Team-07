package com.cooknect.recipe_service.controller;

import com.cooknect.recipe_service.model.*;
import com.cooknect.recipe_service.dto.CommentDto;
import com.cooknect.recipe_service.service.RecipeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService svc;
    public RecipeController(RecipeService svc) { this.svc = svc; }

    @GetMapping
    @Operation(summary = "Get all recipes", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Recipe> listAll() { return svc.listAll(); }

    @GetMapping("/id/{recipeId}")
    @Operation(summary = "Get recipe by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public Recipe get(@PathVariable Long recipeId) { return svc.getById(recipeId); }
    //Fetch recipes of a particular user

    @GetMapping("/{username}")
    @Operation(summary = "Get all recipes by username", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Recipe> getByUsername(@PathVariable String username){
        return  svc.getByUsername(username);
    }

    // Fetch a specific recipe by username and ID
    @GetMapping("/{username}/recipes/{recipeId}")
    @Operation(summary = "Get recipe by username and ID", security = @SecurityRequirement(name = "bearerAuth"))
    public Recipe getByUsernameAndId(@PathVariable String username, @PathVariable Long recipeId) {
        return svc.getByUsernameAndId(username, recipeId);
    }
    //Delete a Recipe based on the username
    @DeleteMapping("/{username}/recipe/{recipeId}")
    @Operation(summary = "Delete a recipe by username and ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteRecipeByUser(
            @PathVariable String username,
            @PathVariable Long recipeId) {

        svc.deleteRecipeByUser(username, recipeId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{username}")
    @Operation(summary = "Delete all recipes by username", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteAllRecipesByUser(@PathVariable String username) {
        svc.deleteAllByUser(username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{oldUsername}/update-username")
    @Operation(summary = "Update username", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<String> updateUsername(
            @PathVariable String oldUsername,
            @RequestBody Map<String, String> requestBody) {

        String newUsername = requestBody.get("newUsername");

        if (newUsername == null || newUsername.isEmpty()) {
            return ResponseEntity.badRequest().body("New username cannot be empty");
        }

        svc.updateUsername(oldUsername, newUsername);
        return ResponseEntity.ok("Username updated successfully from " + oldUsername + " to " + newUsername);
    }

    //Create a recipe
    @PostMapping
    @Operation(summary = "Create a new recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Recipe> create(@RequestBody Recipe recipe, HttpServletRequest request) {
        String username = request.getHeader("X-User-Name");

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Attach username from header to recipe entity
        recipe.setUsername(username);

        Recipe saved = svc.create(recipe);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public Recipe update(@PathVariable Long id, @RequestBody Recipe recipe) {
        return svc.update(id, recipe);
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Patch update recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public Recipe patchUpdate(@PathVariable Long id, @RequestBody Recipe updates) {
        return svc.patchUpdate(id, updates);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a recipe by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{recipeId}/like")
    @Operation(summary = "Like a recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public Recipe like(@PathVariable Long recipeId) {
        return svc.like(recipeId);
    }

    @PostMapping("/{recipeId}/comments")
    @Operation(summary = "Add a comment to a recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public Recipe comment(@PathVariable Long recipeId, @RequestBody CommentDto dto) {
        Comment comment = new Comment();
        comment.setAuthor(dto.getAuthor());
        comment.setText(dto.getText());
        return svc.addComment(recipeId, comment);
    }

    @GetMapping("/search")
    @Operation(summary = "Search a recipe by title", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Recipe> search(@RequestParam String q) {
        return svc.searchByTitle(q);
    }

    @GetMapping("/cuisine/{type}")
    @Operation(summary = "Get all recipes by cuisine", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Recipe> byCuisine(@PathVariable String type) {
        Cuisine c;
        try {
            c = Cuisine.valueOf(type.toUpperCase());
        } catch (Exception e) {
            c = Cuisine.OTHER;
        }
        return svc.findByCuisine(c);
    }

    @GetMapping("/ingredient")
    @Operation(summary = "Get all recipes by ingredient", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Recipe> byIngredient(@RequestParam String q) {
        return svc.findByIngredient(q);
    }

    private static final String TOPIC = "recipe-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/publish")
    @Operation(summary = "Publish a recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public String publishRecipe(@RequestParam String recipeId) {
        String message = "Recipe Published: " + recipeId;
        kafkaTemplate.send(TOPIC, message);
        return "Published: " + message;
    }
}
