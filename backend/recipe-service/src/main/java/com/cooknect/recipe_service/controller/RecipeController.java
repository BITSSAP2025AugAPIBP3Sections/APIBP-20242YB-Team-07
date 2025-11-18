package com.cooknect.recipe_service.controller;

import com.cooknect.recipe_service.dto.CreateCommentDto;
import com.cooknect.recipe_service.dto.GetRecipeDTO;
import com.cooknect.recipe_service.dto.RecipeCreateDTO;

import com.cooknect.recipe_service.model.*;
import com.cooknect.recipe_service.service.RecipeService;
import com.cooknect.recipe_service.service.SpeechSynthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

// Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService svc;
    private final SpeechSynthService speechSynth;
    private static final Logger log = LoggerFactory.getLogger(RecipeController.class);

    public RecipeController(RecipeService svc, SpeechSynthService speechSynth) {
        this.svc = svc;
        this.speechSynth = speechSynth;
    }

    /* Create a new recipe */
    @PostMapping
    @Operation(summary = "Create a new recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Recipe> create(@RequestBody RecipeCreateDTO recipe, HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");

        Long id;
        try {
            id = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(null);
        }

        Recipe saved = svc.create(recipe, id);
        return ResponseEntity.ok(saved);
    }

    /* Like or Unlike a recipe */
    @PostMapping("/{recipeId}/like")
    @Operation(summary = "Like a recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> like(@PathVariable Long recipeId, HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");

        Long id;
        try {
            id = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(null);
        }

        svc.likeAndUnlike(recipeId, id);
        return ResponseEntity.noContent().build();
    }

    /* Get all recipes */
    @GetMapping
    @Operation(summary = "Get all recipes", security = @SecurityRequirement(name = "bearerAuth"))
    public List<GetRecipeDTO> listAll(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        Long id;
        try {
            id = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return List.of();
        }
        return svc.getAllRecipes(id);
    }
    /* Get recipe by ID */
    @GetMapping("/{recipeId}")
    @Operation(summary = "Get recipe by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<GetRecipeDTO> getById(
            @PathVariable Long recipeId,
            HttpServletRequest request
    ) {
        String userIdHeader = request.getHeader("X-User-Id");

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        GetRecipeDTO dto = svc.getById(recipeId, userId);
        return ResponseEntity.ok(dto);
    }

    /* Add Comment for a recipe */
    @PostMapping("/{recipeId}/comments")
    @Operation(
            summary = "Add a comment to a recipe",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> addComment(
            @PathVariable Long recipeId,
            @RequestBody CreateCommentDto dto,
            HttpServletRequest request
    ) {
        String userIdHeader = request.getHeader("X-User-Id");

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        Comment comment = new Comment();
        comment.setAuthor(String.valueOf(userId));   // Author = userId from header
        comment.setText(dto.getText());

        svc.addComment(recipeId, comment);

        return ResponseEntity.noContent().build();
    }

    private static final String TOPIC = "recipe-topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // Publish a recipe message to Kafka topic
    @PostMapping("/publish")
    @Operation(summary = "Publish a recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public String publishRecipe(@RequestParam String recipeId) {
        String message = "Recipe Published: " + recipeId;
        kafkaTemplate.send(TOPIC, message);
        return "Published: " + message;
    }

    // Search recipes by title
    @GetMapping("/search")
    @Operation(summary = "Search a recipe by title", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Recipe> search(@RequestParam String q) {
        return svc.searchByTitle(q);
    }

    // Get recipes by cuisine type
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

    // Get recipes containing a specific ingredient
    @GetMapping("/ingredient")
    @Operation(summary = "Get all recipes by ingredient", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Recipe> byIngredient(@RequestParam String q) {
        return svc.findByIngredient(q);
    }

    @GetMapping(value = "/{id}/speak", produces = "audio/wav")
    public ResponseEntity<byte[]> speakRecipe(@PathVariable Long id,
                                            @RequestParam(required = false) String voice ) {
        try {
            Recipe recipe = svc.getRecipeById(id);

            String text;
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                text = mapper.writeValueAsString(recipe);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                text = recipe.toString();
            }

            // use service that checks DB, generates, saves
            byte[] wav = speechSynth.getOrCreateAudio(text, voice, id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .header("Content-Disposition", "inline; filename=\"recipe-" + id + ".wav\"")
                    .body(wav);

        } catch (com.cooknect.recipe_service.exception.NotFoundException nf) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to produce audio for recipe {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(503).body(null);
        }

    }

//    // Update an existing recipe
//    @PutMapping("/{id}")
//    @Operation(summary = "Update a recipe", security = @SecurityRequirement(name = "bearerAuth"))
//    public Recipe update(@PathVariable Long id, @RequestBody Recipe recipe) {
//        return svc.update(id, recipe);
//    }


    /* Update the Recipe */
    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Recipe> patchRecipe(
            @PathVariable Long id,
            @RequestBody Recipe recipe,
            HttpServletRequest request
    ) {
        String userIdHeader = request.getHeader("X-User-Id");

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        Recipe updated = svc.patchUpdate(id, recipe, userId);
        return ResponseEntity.ok(updated);
    }


    // Delete a specific recipe by userId and recipe ID
    @DeleteMapping("/{recipeId}")
    @Operation(summary = "Delete a recipe by user ID and recipe ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteRecipeByUser(
            @PathVariable Long recipeId,
            HttpServletRequest request
    ) {
        // Fetch userId from header
        String userIdHeader = request.getHeader("X-User-Id");
        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        // Call service method to delete the recipe
        svc.deleteRecipeByUser(userId, recipeId);

        return ResponseEntity.noContent().build();
    }

    // Delete all recipes by userId
    @DeleteMapping
    @Operation(summary = "Delete all recipes by user ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteAllRecipesByUser(HttpServletRequest request) {
        // Fetch userId from header
        String userIdHeader = request.getHeader("X-User-Id");
        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        // Call service method to delete all recipes for this user
        svc.deleteAllByUser(userId);

        return ResponseEntity.noContent().build();
    }



}