package com.cooknect.recipe_service.controller;

import com.cooknect.recipe_service.model.*;
import com.cooknect.recipe_service.dto.CommentDto;
import com.cooknect.recipe_service.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PatchMapping;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService svc;
    public RecipeController(RecipeService svc) { this.svc = svc; }

    @GetMapping
    public List<Recipe> listAll() { return svc.listAll(); }

    @GetMapping("/{id}")
    public Recipe get(@PathVariable Long id) { return svc.getById(id); }

    @PostMapping
    public ResponseEntity<Recipe> create(@RequestBody Recipe recipe) {
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

    @PostMapping("/{id}/like")
    public Recipe like(@PathVariable Long id) {
        return svc.like(id);
    }

    @PostMapping("/{id}/comments")
    public Recipe comment(@PathVariable Long id, @RequestBody CommentDto dto) {
        Comment comment = new Comment();
        comment.setAuthor(dto.getAuthor());
        comment.setText(dto.getText());
        return svc.addComment(id, comment);
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
