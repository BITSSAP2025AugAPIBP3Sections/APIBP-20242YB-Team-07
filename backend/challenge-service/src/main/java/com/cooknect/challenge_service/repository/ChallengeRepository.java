package com.cooknect.challenge_service.repository;

import com.cooknect.challenge_service.model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    // Custom query methods can be added here if needed
}

