package com.cooknect.challenge_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "challenge_recipe_submissions")
public class ChallengeRecipeSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private Long recipeId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime submissionTime;

}
