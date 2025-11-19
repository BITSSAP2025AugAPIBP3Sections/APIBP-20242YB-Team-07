package com.cooknect.recipe_service.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Embeddable
public class PreparationStep {
    private String step;
}
