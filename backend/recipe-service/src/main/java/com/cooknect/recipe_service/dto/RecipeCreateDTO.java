package com.cooknect.recipe_service.dto;

import com.cooknect.recipe_service.model.Ingredient;
import com.cooknect.recipe_service.model.PreparationStep;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeCreateDTO {
    private String title;
    private String description;
    private List<Ingredient> ingredients;
    private List<PreparationStep> preparation;
    private String cuisine;
    private String recipeImageUrl;
    
    // New tribute fields
    @JsonProperty("isTribute")
    private boolean isTribute;
    private String authorName;  // Available only when isTribute is true
    private String tributeDescription;  // Additional description for tribute
    private String tributeImageUrl;  // Photo URL for tribute
}
