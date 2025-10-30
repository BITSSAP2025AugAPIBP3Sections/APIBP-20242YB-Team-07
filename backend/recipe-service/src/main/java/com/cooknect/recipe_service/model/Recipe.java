package com.cooknect.recipe_service.model;
import com.cooknect.recipe_service.model.Ingredient;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 4000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    private List<Ingredient> ingredients = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Cuisine cuisine = Cuisine.OTHER;

    private String language = "en";
    private int likes = 0;

    private String createdBy; // store creator id or email

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<Comment> comments = new ArrayList<>();
}
