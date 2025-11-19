package com.cooknect.recipe_service.dto;

import com.cooknect.recipe_service.model.Ingredient;
import com.cooknect.recipe_service.model.PreparationStep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetRecipeDTO {
    private Long id;
    private String title;
    private String description;
    private String cuisine;
    private String recipeImageUrl;
    private List<GetCommentDto> comments;
    private List<Ingredient> ingredients;
    private List<PreparationStep> preparation;
    private int likesCount;
    private boolean likedByUser;
    private boolean savedByUser;
    private String username;
    private int commentCount;
    private Long userId;
}
