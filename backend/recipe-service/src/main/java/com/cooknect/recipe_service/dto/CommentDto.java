package com.cooknect.recipe_service.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CommentDto {
    private String author;
    private String text;
}
