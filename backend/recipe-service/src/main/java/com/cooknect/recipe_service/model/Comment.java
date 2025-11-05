package com.cooknect.recipe_service.model;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author; // string user id/email
    @Column(length = 2000)
    private String text;
    private Instant createdAt = Instant.now();

}
