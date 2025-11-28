package com.cooknect.recipe_service.controller;

import com.cooknect.common.events.RecipeEvent;
import com.cooknect.recipe_service.dto.CreateCommentDto;
import com.cooknect.recipe_service.dto.GetRecipeDTO;
import com.cooknect.recipe_service.dto.RecipeCreateDTO;
import com.cooknect.common.dto.PageRequestDTO;
import com.cooknect.common.dto.PageResponseDTO;
import com.cooknect.recipe_service.event.RecipeEventProducer;
import com.cooknect.recipe_service.model.*;
import com.cooknect.recipe_service.service.RecipeService;
import com.cooknect.recipe_service.service.SpeechSynthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;


// Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService svc;
    private final SpeechSynthService speechSynth;
    private static final Logger log = LoggerFactory.getLogger(RecipeController.class);

    @Autowired
    private RecipeEventProducer recipeEventProducer;

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

        Map<String, Object> user = svc.getUserDetailsById(id);
        RecipeEvent event = new RecipeEvent(
            user.get("email").toString(),
            "New Recipe Created Successfully",
            String.format("Hi %s! \nYou have successfully created a new recipe on Cooknect.", user.get("fullName").toString())
        );
        recipeEventProducer.sendRecipeEvent(event);
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
        Map<String, Object> user = svc.getUserDetailsById(id);
        RecipeEvent event = new RecipeEvent(
            user.get("email").toString(),
            "Recipe Liked/Unliked Successfully",
            String.format("Hi %s! \nYou have successfully liked/unliked a recipe on Cooknect.", user.get("fullName").toString())
        );
        recipeEventProducer.sendRecipeEvent(event);
        return ResponseEntity.noContent().build();
    }

    /* Save or Unsave a recipe */
    @PostMapping("/{recipeId}/save")
    @Operation(summary = "Save or Unsave a recipe", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> saveRecipe(@PathVariable Long recipeId, HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid user id");
        }

        svc.saveAndUnsave(recipeId, userId);
        Map<String, Object> user = svc.getUserDetailsById(userId);
        RecipeEvent event = new RecipeEvent(
            user.get("email").toString(),
            "Saved/Unsaved Recipe Successfully",
            String.format("Hi %s! \nYou have successfully saved/unsaved a recipe on Cooknect.", user.get("fullName").toString())
        );
        recipeEventProducer.sendRecipeEvent(event);
        return ResponseEntity.noContent().build();
    }


    /* Get all recipes */
    // Add title as an optional query parameter
    @GetMapping
    @Operation(summary = "Get all recipes optionally by userId, saved status, and title", security = @SecurityRequirement(name = "bearerAuth"))
    public PageResponseDTO<GetRecipeDTO> listAll(
            HttpServletRequest request,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean saved,
            @Parameter(
                    name = "title",
                    description = "Recipe title"
            )
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(
                    name = "sortBy",
                    description = "Sort field",
                    schema = @Schema(allowableValues = {"id", "title", "description", "cuisine"})
            )
            @RequestParam(defaultValue = "id", name = "sortBy", required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        PageRequestDTO pageRequestDTO = new PageRequestDTO();
        pageRequestDTO.setPage(page - 1);
        pageRequestDTO.setSize(size);
        pageRequestDTO.setSortBy(sortBy);
        pageRequestDTO.setDirection(direction);

        String userIdHeader = request.getHeader("X-User-Id");
        Long authenticatedUserId = null;
        if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
            try {
                authenticatedUserId = Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header value: {}", userIdHeader);
            }
        }

        PageResponseDTO<GetRecipeDTO> result;
        if (title != null && !title.trim().isEmpty()) {
            // Filter by title (and optionally userId/saved)
            if(userId == null) {
                userId = authenticatedUserId;
            }
            result = svc.getRecipesByTitle(title, userId, saved, pageRequestDTO);
        } else if (userId != null && Boolean.TRUE.equals(saved)) {
            result = svc.getSavedRecipesByUserId(userId, pageRequestDTO);
        } else if (userId != null) {
            result = svc.getRecipesByUserId(userId, pageRequestDTO);
        } else {
            result = svc.getAllRecipes(authenticatedUserId, pageRequestDTO);
        }

        result.setPage(result.getPage() + 1);
        return result;
    }



    /* Get recipe by ID */
    @GetMapping("/{recipeId}")
    @Operation(summary = "Get recipe by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<GetRecipeDTO> getById(
            @PathVariable Long recipeId,
            HttpServletRequest request
    ) {
        String userIdHeader = request.getHeader("X-User-Id");
        Long userId = null;
        
        // Try to parse the user ID from header, but allow null if not present or invalid
        if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
            try {
                userId = Long.parseLong(userIdHeader);
            } catch (NumberFormatException e) {
                // Log warning but continue with null user ID
                log.warn("Invalid X-User-Id header value: {}", userIdHeader);
            }
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
        Map<String, Object> user = svc.getUserDetailsById(userId);
        RecipeEvent event = new RecipeEvent(
            user.get("email").toString(),
            "New Comment Added Successfully",
            String.format("Hi %s! \nYou have successfully added a new comment on Cooknect.", user.get("fullName").toString())
        );
        recipeEventProducer.sendRecipeEvent(event);

        return ResponseEntity.noContent().build();
    }

    /*
         * Searches for recipes whose titles match the given query string.
         * Returns the recipe with the maximum number of likes among all matches.
     */

    @GetMapping("/search")
    @Operation(summary = "Search for the most liked recipe by title", 
               description = "Returns the recipe with the highest number of likes among all recipes matching the search query.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public List<Recipe> search(@RequestParam String q) {
        return svc.searchByTitle(q);
    }

    /*
          * Retrieves all recipes for a given cuisine type.
          * The provided cuisine string is converted to a valid enum value;
          * if the conversion fails, the cuisine is defaulted to Cuisine.OTHER.
     */

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

   /*
        * Fetch all the Recipes containing a specific ingredient
    */
    @GetMapping("/ingredient")
    @Operation(summary = "Get all recipes by ingredient", security = @SecurityRequirement(name = "bearerAuth"))
    public List<Recipe> byIngredient(@RequestParam String q) {
        return svc.findByIngredient(q);
    }

    @GetMapping(value = "/{id}/speak", produces = "audio/wav")
    public ResponseEntity<byte[]> speakRecipe(@PathVariable Long id,
                                            @RequestParam(required = false) String voice, @PathVariable (required = false) String language) {
        try {
            Recipe recipe = svc.getRecipeById(id);

            // Prepare text to be spoken
            Map<String, Object> textMap = Map.of(
                    "title", recipe.getTitle(),
                    "description", recipe.getDescription(),
                    "ingredients", recipe.getIngredients(),
                    "preparation", recipe.getPreparation()
            );

            String text;
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                text = mapper.writeValueAsString(textMap);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                text = textMap.toString();
            }

            // use service that checks DB, generates, saves
            byte[] wav = speechSynth.getOrCreateAudio(text, voice, id, language);

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

    //Translate Recipe into target language
    @GetMapping("/{id}/translate/{targetLanguage}")
    @Operation(summary = "Translate recipe text to target language", security = @SecurityRequirement(name = "bearerAuth"), hidden = true )
    public ResponseEntity<byte[]> translateRecipe(
            @PathVariable Long id,
            @PathVariable String targetLanguage) {
        String translated_text = speechSynth.translateText(id, targetLanguage);
        byte[] wav = speechSynth.getOrCreateAudio(translated_text, "Kore", id, targetLanguage);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header("Content-Disposition", "inline; filename=\"recipe-" + id + ".wav\"")
                .body(wav);
    }

    /*
        * Updates an existing recipe.
        * This endpoint accepts a PATCH request with only the fields that need to be updated.
        * The user ID is extracted from the "X-User-Id" request header to validate update permissions.
     */

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
        Map<String, Object> user = svc.getUserDetailsById(id);
        RecipeEvent event = new RecipeEvent(
            user.get("email").toString(),
            "Recipe Updated Successfully",
            String.format("Hi %s! \nYou have successfully updated a recipe on Cooknect.", user.get("fullName").toString())
        );
        recipeEventProducer.sendRecipeEvent(event);
        speechSynth.deleteAudioForRecipe(id);
        return ResponseEntity.ok(updated);
    }


    /*
        * Delete a specific recipe by userId and recipe ID
        * It checks the recipe deleted by the user belongs to that particular user only
     */
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

        Map<String, Object> user = svc.getUserDetailsById(userId);
        RecipeEvent event = new RecipeEvent(
            user.get("email").toString(),
            "Recipe Deleted Successfully",
            String.format("Hi %s! \nYou have successfully deleted a recipe on Cooknect.", user.get("fullName").toString())
        );
        recipeEventProducer.sendRecipeEvent(event);
        // Call service method to delete the recipe
        svc.deleteRecipeByUser(userId, recipeId);

        return ResponseEntity.noContent().build();
    }
    /*
        * Deleting all Recipes of the user as user account has been deleted.
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete all recipes by user ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteAllRecipesByUser(
            @PathVariable Long userId,
            HttpServletRequest request) {
        // Fetch userId from header
        String userIdHeader = request.getHeader("X-User-Id");
        Long userIdHeaderLong;
        try {
            userIdHeaderLong = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        if(!userIdHeaderLong.equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Map<String, Object> user = svc.getUserDetailsById(userIdHeaderLong);
        RecipeEvent event = new RecipeEvent(
            user.get("email").toString(),
            "Recipes Deleted Successfully",
            String.format("Hi %s! \nYou have successfully deleted all your recipes on Cooknect.", user.get("fullName").toString())
        );
        recipeEventProducer.sendRecipeEvent(event);
        // Call service method to delete all recipes for this user
        svc.deleteAllByUser(userId);

        return ResponseEntity.noContent().build();
    }
}