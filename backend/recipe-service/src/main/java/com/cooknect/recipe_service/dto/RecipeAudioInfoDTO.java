package com.cooknect.recipe_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeAudioInfoDTO {
    private Long id;
    private String language;
    private String contentType;
}