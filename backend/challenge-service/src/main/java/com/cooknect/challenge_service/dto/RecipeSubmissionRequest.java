package com.cooknect.challenge_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeSubmissionRequest {
    private Long recipeId;
    private String userId;
}
