package com.cooknect.challenge_service.controller;

import com.cooknect.challenge_service.dto.CreateChallengeRequest;
import com.cooknect.challenge_service.dto.ChallengeResponse;
import com.cooknect.challenge_service.dto.UpdateChallengeRequest;
import com.cooknect.challenge_service.dto.ChallengeParticipationRequest;
import com.cooknect.challenge_service.model.ChallengeParticipant;
import com.cooknect.challenge_service.service.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;

    @Autowired
    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @PostMapping
    public ResponseEntity<ChallengeResponse> createChallenge(@RequestBody CreateChallengeRequest request) {
        ChallengeResponse response = challengeService.createChallenge(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ChallengeResponse>> getAllChallenges() {
        List<ChallengeResponse> challenges = challengeService.getAllChallenges();
        return ResponseEntity.ok(challenges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeResponse> getChallengeById(@PathVariable Long id) {
        ChallengeResponse response = challengeService.getChallengeById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ChallengeResponse> updateChallenge(@PathVariable Long id, @RequestBody UpdateChallengeRequest request) {
        ChallengeResponse response = challengeService.updateChallenge(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChallenge(@PathVariable Long id) {
        challengeService.deleteChallenge(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{challengeId}/join")
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
    public ResponseEntity<List<ChallengeParticipant>> getChallengeParticipants(@PathVariable Long challengeId) {
        List<ChallengeParticipant> participants = challengeService.getChallengeParticipants(challengeId);
        return ResponseEntity.ok(participants);
    }
}
