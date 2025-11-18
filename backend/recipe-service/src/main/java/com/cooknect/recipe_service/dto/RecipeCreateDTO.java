package com.cooknect.recipe_service.dto;

import com.cooknect.recipe_service.model.Ingredient;
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
    private String cuisine;
    private String recipeImageUrl;
}
