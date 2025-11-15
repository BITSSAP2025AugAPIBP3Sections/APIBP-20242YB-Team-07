package com.cooknect.recipe_service.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "recipe_audio")
public class RecipeAudio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Lob
    @Column(name = "audio_data", nullable = false)
    private byte[] audioData;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public RecipeAudio() {}

    public RecipeAudio(Long recipeId, byte[] audioData, String contentType) {
        this.recipeId = recipeId;
        this.audioData = audioData;
        this.contentType = contentType;
        this.createdAt = Instant.now();
    }

    // getters / setters
    public Long getId() { return id; }
    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }
    public byte[] getAudioData() { return audioData; }
    public void setAudioData(byte[] audioData) { this.audioData = audioData; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}