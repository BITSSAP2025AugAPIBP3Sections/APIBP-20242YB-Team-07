package com.cooknect.challenge_service.service;

import com.cooknect.challenge_service.dto.CreateChallengeRequest;
import com.cooknect.challenge_service.dto.ChallengeResponse;
import com.cooknect.challenge_service.model.Challenge;
import com.cooknect.challenge_service.model.ChallengeStatus;
import com.cooknect.challenge_service.repository.ChallengeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;

    @Autowired
    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    public ChallengeResponse createChallenge(CreateChallengeRequest request) {
        Challenge challenge = new Challenge();
        challenge.setName(request.getName());
        challenge.setDescription(request.getDescription());
        challenge.setStartDate(request.getStartDate());
        challenge.setEndDate(request.getEndDate());
        challenge.setType(request.getType());
        challenge.setStatus(ChallengeStatus.UPCOMING);
        Challenge saved = challengeRepository.save(challenge);
        return toResponse(saved);
    }

    private ChallengeResponse toResponse(Challenge challenge) {
        ChallengeResponse response = new ChallengeResponse();
        response.setId(challenge.getId());
        response.setName(challenge.getName());
        response.setDescription(challenge.getDescription());
        response.setStartDate(challenge.getStartDate());
        response.setEndDate(challenge.getEndDate());
        response.setType(challenge.getType());
        response.setStatus(challenge.getCurrentStatus()); // Use dynamic status
        return response;
    }
    
    public List<ChallengeResponse> getAllChallenges() {
        return challengeRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
