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

    private String description;

    private String recipeImageUrl;

    @ElementCollection
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    private List<Ingredient> ingredients = new ArrayList<>();


    @ElementCollection
    @CollectionTable(
            name = "recipe_preparation_steps",
            joinColumns = @JoinColumn(name = "recipe_id")
    )
    private List<PreparationStep> preparation = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Cuisine cuisine = Cuisine.OTHER;

    private String language = "en";
    private int likes = 0;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<Like> detailLikes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<SavedRecipe> savedByUsers = new ArrayList<>();



    private Long userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<Comment> comments = new ArrayList<>();
    
    // Tribute fields
    @Column(name = "is_tribute", columnDefinition = "boolean default false", nullable = false)
    private boolean isTribute = false;
    
    @Column(name = "author_name")
    private String authorName;  // Available only when isTribute is true
    
    @Column(name = "tribute_description", columnDefinition = "TEXT")
    private String tributeDescription;  // Additional description for tribute
    
    @Column(name = "tribute_image_url")
    private String tributeImageUrl;  // Photo URL for tribute
}
