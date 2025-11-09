package com.cooknect.nutrition_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "food_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_item", nullable = false)
    @JsonProperty("name")
    private String foodItem;

    @Column(name = "serving_size", nullable = false)
    @JsonProperty("serving_size_g")
    private String servingSize;

    @Column(name = "total_fat")
    @JsonProperty("fat_total_g")
    private Double totalFat;

    @Column(name = "saturated_fat")
    @JsonProperty("fat_saturated_g")
    private Double saturatedFat;

    @Column(name = "sodium")
    @JsonProperty("sodium_mg")
    private Double sodium;

    @Column(name = "potassium")
    @JsonProperty("potassium_mg")
    private Double potassium;

    @Column(name = "cholestrol")
    @JsonProperty("cholesterol_mg")
    private Double cholestrol;

    @Column(name = "carbohydrates")
    @JsonProperty("carbohydrates_total_g")
    private Double carbohydrates;

    @Column(name = "fiber")
    @JsonProperty("fiber_g")
    private Double fiber;

    @Column(name = "sugar")
    @JsonProperty("sugar_g")
    private Double sugar;
}