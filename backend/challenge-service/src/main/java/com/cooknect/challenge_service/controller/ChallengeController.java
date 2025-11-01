package com.cooknect.challenge_service.controller;

import com.cooknect.challenge_service.dto.CreateChallengeRequest;
import com.cooknect.challenge_service.dto.ChallengeResponse;
import com.cooknect.challenge_service.service.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}

