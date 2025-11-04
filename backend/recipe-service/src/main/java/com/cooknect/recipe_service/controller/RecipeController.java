package com.cooknect.recipe_service.controller;

import com.cooknect.recipe_service.model.*;
import com.cooknect.recipe_service.dto.CommentDto;
import com.cooknect.recipe_service.service.RecipeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PatchMapping;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService svc;
    public RecipeController(RecipeService svc) { this.svc = svc; }



    @GetMapping
    public List<Recipe> listAll() { return svc.listAll(); }

    @GetMapping("/id/{recipeId}")
    public Recipe get(@PathVariable Long recipeId) { return svc.getById(recipeId); }
    //Fetch recipes of a particular user

    @GetMapping("/{username}/allrecipes")
    public List<Recipe> getByUsername(@PathVariable String username){
        return  svc.getByUsername(username);
    }

    // ✅ Fetch a specific recipe by username and ID
    @GetMapping("/{username}/recipes/{recipeId}")
    public Recipe getByUsernameAndId(@PathVariable String username, @PathVariable Long recipeId) {
        return svc.getByUsernameAndId(username, recipeId);
    }
    //Delete a Recipe based on the username
    @DeleteMapping("/{username}/recipe/{recipeId}")
    public ResponseEntity<Void> deleteRecipeByUser(
            @PathVariable String username,
            @PathVariable Long recipeId) {

        svc.deleteRecipeByUser(username, recipeId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{username}/delete")
    public ResponseEntity<Void> deleteAllRecipesByUser(@PathVariable String username) {
        svc.deleteAllByUser(username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{oldUsername}/update-username")
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
    public Recipe update(@PathVariable Long id, @RequestBody Recipe recipe) {
        return svc.update(id, recipe);
    }
    @PatchMapping("/{id}")
    public Recipe patchUpdate(@PathVariable Long id, @RequestBody Recipe updates) {
        return svc.patchUpdate(id, updates);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{recipeId}/like")
    public Recipe like(@PathVariable Long recipeId) {
        return svc.like(recipeId);
    }

    @PostMapping("/{recipeId}/comments")
    public Recipe comment(@PathVariable Long recipeId, @RequestBody CommentDto dto) {
        Comment comment = new Comment();
        comment.setAuthor(dto.getAuthor());
        comment.setText(dto.getText());
        return svc.addComment(recipeId, comment);
    }

    @GetMapping("/search")
    public List<Recipe> search(@RequestParam String q) {
        return svc.searchByTitle(q);
    }

    @GetMapping("/cuisine/{type}")
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
    public List<Recipe> byIngredient(@RequestParam String q) {
        return svc.findByIngredient(q);
    }



}
