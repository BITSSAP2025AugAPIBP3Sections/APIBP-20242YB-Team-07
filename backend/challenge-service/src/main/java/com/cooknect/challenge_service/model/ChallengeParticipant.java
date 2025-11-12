package com.cooknect.challenge_service.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@Embeddable
public class ChallengeParticipant {
    private Long userId;
    private String username;
    private String email;
    private String role;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeParticipant that = (ChallengeParticipant) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(username, that.username) &&
               Objects.equals(email, that.email) &&
               Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, role);
    }
}