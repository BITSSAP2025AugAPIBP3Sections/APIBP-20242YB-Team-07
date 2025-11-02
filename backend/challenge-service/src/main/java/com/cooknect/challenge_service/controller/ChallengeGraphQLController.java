package com.cooknect.challenge_service.controller;

import com.cooknect.challenge_service.dto.*;
import com.cooknect.challenge_service.model.ChallengeParticipant;
import com.cooknect.challenge_service.service.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ChallengeGraphQLController {
    private final ChallengeService challengeService;

    @Autowired
    public ChallengeGraphQLController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @MutationMapping
    public ChallengeResponse createChallenge(@Argument CreateChallengeRequest request) {
        return challengeService.createChallenge(request);
    }
    
    @QueryMapping
    public List<ChallengeResponse> challenges() {
        return challengeService.getAllChallenges();
    }

    @QueryMapping
    public ChallengeResponse challenge(@Argument Long id) {
        return challengeService.getChallengeById(id);
    }

    @MutationMapping
    public ChallengeResponse updateChallenge(@Argument Long id, @Argument UpdateChallengeRequest request) {
        return challengeService.updateChallenge(id, request);
    }

    @MutationMapping
    public Boolean deleteChallenge(@Argument Long id) {
        return challengeService.deleteChallenge(id);
    }

    @MutationMapping
    public Boolean joinChallenge(@Argument Long challengeId, @Argument ChallengeParticipationRequest request) {
        return challengeService.joinChallenge(challengeId, request);
    }

    @MutationMapping
    public Boolean leaveChallenge(@Argument Long challengeId, @Argument ChallengeParticipationRequest request) {
        return challengeService.leaveChallenge(challengeId, request);
    }

    @QueryMapping
    public List<ChallengeParticipant> challengeParticipants(@Argument Long challengeId) {
        return challengeService.getChallengeParticipants(challengeId);
    }
    @MutationMapping
    public Boolean submitRecipeToChallenge(@Argument Long challengeId, @Argument RecipeSubmissionRequest request) {
        return challengeService.submitRecipeToChallenge(challengeId, request);
    }
    @QueryMapping
    public List<LeaderboardEntry> challengeLeaderboard(@Argument Long challengeId) {
        return challengeService.getChallengeLeaderboard(challengeId);
    }
}
