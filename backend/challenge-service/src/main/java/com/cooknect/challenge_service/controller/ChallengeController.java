package com.cooknect.challenge_service.controller;

import com.cooknect.challenge_service.dto.*;
import com.cooknect.challenge_service.model.ChallengeParticipant;
import com.cooknect.challenge_service.service.ChallengeService;
import com.cooknect.common.dto.PageRequestDTO;
import com.cooknect.common.dto.PageResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;

    @Autowired
    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @PostMapping
    @Operation(summary = "Create a new challenge", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChallengeResponse> createChallenge(@RequestBody CreateChallengeRequest request) {
        ChallengeResponse response = challengeService.createChallenge(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all challenges with pagination", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PageResponseDTO<ChallengeResponse>> getAllChallenges(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        PageRequestDTO pageRequestDTO = new PageRequestDTO();
        pageRequestDTO.setPage(page - 1); // Convert to 0-based for internal processing
        pageRequestDTO.setSize(size);
        pageRequestDTO.setSortBy(sortBy);
        pageRequestDTO.setDirection(direction);
        
        PageResponseDTO<ChallengeResponse> result = challengeService.getAllChallenges(pageRequestDTO);
        result.setPage(result.getPage() + 1); // Convert back to 1-based for response
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get challenge by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChallengeResponse> getChallengeById(@PathVariable Long id) {
        ChallengeResponse response = challengeService.getChallengeById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update challenge by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ChallengeResponse> updateChallenge(@PathVariable Long id, @RequestBody UpdateChallengeRequest request) {
        ChallengeResponse response = challengeService.updateChallenge(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete challenge by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteChallenge(@PathVariable Long id) {
        challengeService.deleteChallenge(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{challengeId}/join")
    @Operation(summary = "Join a challenge", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> joinChallenge(@PathVariable Long challengeId, @RequestBody ChallengeParticipationRequest request) {
        boolean joined = challengeService.joinChallenge(challengeId, request);
        Map<String, String> response = new HashMap<>();
        if (joined) {
            response.put("message", "User joined challenge successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "User already joined this challenge.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{challengeId}/leave")
    @Operation(summary = "Leave a challenge", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> leaveChallenge(
            @PathVariable Long challengeId,
            @RequestBody ChallengeParticipationRequest request) {

        boolean left = challengeService.leaveChallenge(challengeId, request);

        Map<String, String> response = new HashMap<>();

        if (left) {
            response.put("message", "User left challenge successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "User was not a participant in this challenge.");
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/{challengeId}/participants")
    @Operation(summary = "Get all participants in a challenge", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<ChallengeParticipant>> getChallengeParticipants(@PathVariable Long challengeId) {
        List<ChallengeParticipant> participants = challengeService.getChallengeParticipants(challengeId);
        return ResponseEntity.ok(participants);
    }
    
    @PostMapping("/{challengeId}/submit-recipe")
    @Operation(summary = "Submit a recipe to a challenge", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> submitRecipeToChallenge(@PathVariable Long challengeId, @RequestBody RecipeSubmissionRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = challengeService.submitRecipeToChallenge(challengeId, request);
            if (success) {
                response.put("message", "Recipe submitted successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Recipe submission failed.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (RuntimeException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{challengeId}/leaderboard")
    @Operation(summary = "Get challenge leaderboard", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<LeaderboardEntry>> getChallengeLeaderboard(@PathVariable Long challengeId) {
        List<LeaderboardEntry> leaderboard = challengeService.getChallengeLeaderboard(challengeId);
        return ResponseEntity.ok(leaderboard);
    }
}
