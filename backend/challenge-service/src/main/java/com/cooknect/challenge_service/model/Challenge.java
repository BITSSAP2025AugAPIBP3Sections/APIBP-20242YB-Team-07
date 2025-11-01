package com.cooknect.challenge_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "challenges")
public class Challenge {
    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status;
    
    public ChallengeStatus getCurrentStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (status == ChallengeStatus.CANCELLED) {
            return ChallengeStatus.CANCELLED;
        }
        if (now.isBefore(startDate)) {
            return ChallengeStatus.UPCOMING;
        } else if (now.isAfter(endDate) || now.isEqual(endDate)) {
            return ChallengeStatus.COMPLETED;
        } else {
            return ChallengeStatus.ONGOING;
        }
    }
}
