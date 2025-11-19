package com.cooknect.recipe_service.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GetCommentDto {
    private String author;
    private String text;
}

