package com.cooknect.challenge_service.repository;

import com.cooknect.challenge_service.model.ChallengeRecipeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChallengeRecipeSubmissionRepository extends JpaRepository<ChallengeRecipeSubmission, Long> {
    List<ChallengeRecipeSubmission> findByChallengeId(Long challengeId);
    List<ChallengeRecipeSubmission> findByChallengeIdAndUserId(Long challengeId, String userId);
    List<ChallengeRecipeSubmission> findByChallengeIdAndRecipeId(Long challengeId, Long recipeId);
}
