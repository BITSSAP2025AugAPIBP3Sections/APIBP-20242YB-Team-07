package com.cooknect.challenge_service.controller;

import com.cooknect.challenge_service.dto.*;
import com.cooknect.challenge_service.model.ChallengeParticipant;
import com.cooknect.challenge_service.service.ChallengeService;
import com.cooknect.common.dto.PageRequestDTO;
import com.cooknect.common.dto.PageResponseDTO;
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
    public PageResponseDTO<ChallengeResponse> challenges(
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument String direction
    ) {
        PageRequestDTO pageRequestDTO = new PageRequestDTO();
        pageRequestDTO.setPage((page != null ? page : 1) - 1); // Convert to 0-based for internal processing
        pageRequestDTO.setSize(size != null ? size : 10);
        pageRequestDTO.setSortBy(sortBy != null ? sortBy : "id");
        pageRequestDTO.setDirection(direction != null ? direction : "asc");
        
        PageResponseDTO<ChallengeResponse> result = challengeService.getAllChallenges(pageRequestDTO);
        result.setPage(result.getPage() + 1); // Convert back to 1-based for response
        return result;
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
