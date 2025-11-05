package com.cooknect.recipe_service.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Ingredient {
    private String name;
    private String quantity; // e.g. "2 cups", "1 tsp", "200 g"
}