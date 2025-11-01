package com.cooknect.challenge_service.service;

import com.cooknect.challenge_service.dto.CreateChallengeRequest;
import com.cooknect.challenge_service.dto.ChallengeResponse;
import com.cooknect.challenge_service.dto.UpdateChallengeRequest;
import com.cooknect.challenge_service.dto.ChallengeParticipationRequest;
import com.cooknect.challenge_service.model.Challenge;
import com.cooknect.challenge_service.model.ChallengeStatus;
import com.cooknect.challenge_service.model.ChallengeParticipant;
import com.cooknect.challenge_service.repository.ChallengeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    public ChallengeResponse getChallengeById(Long id) {
        Optional<Challenge> challengeOpt = challengeRepository.findById(id);
        if (challengeOpt.isEmpty()) {
            throw new RuntimeException("Challenge not found");
        }
        return toResponse(challengeOpt.get());
    }

    public ChallengeResponse updateChallenge(Long id, UpdateChallengeRequest request) {
        Optional<Challenge> challengeOpt = challengeRepository.findById(id);
        if (challengeOpt.isEmpty()) {
            throw new RuntimeException("Challenge not found");
        }
        Challenge challenge = challengeOpt.get();
        if (request.getName() != null) challenge.setName(request.getName());
        if (request.getDescription() != null) challenge.setDescription(request.getDescription());
        if (request.getStartDate() != null) challenge.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) challenge.setEndDate(request.getEndDate());
        if (request.getType() != null) challenge.setType(request.getType());
        Challenge updated = challengeRepository.save(challenge);
        return toResponse(updated);
    }

    public boolean deleteChallenge(Long id) {
        if (!challengeRepository.existsById(id)) {
            throw new RuntimeException("Challenge not found");
        }
        challengeRepository.deleteById(id);
        return true;
    }

    public boolean joinChallenge(Long challengeId, ChallengeParticipationRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        boolean alreadyJoined = challenge.getParticipants().stream()
                .anyMatch(p -> p.getUsername().equals(request.getUsername()));

        if (alreadyJoined) {
            return false; // Already joined
        }

        ChallengeParticipant participant = new ChallengeParticipant();
        participant.setUsername(request.getUsername());
        participant.setEmail(request.getEmail());
        participant.setRole(request.getRole());
        challenge.getParticipants().add(participant);
        challengeRepository.save(challenge);
        return true;
    }


    public boolean leaveChallenge(Long challengeId, ChallengeParticipationRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new RuntimeException("Challenge not found"));
        ChallengeParticipant participant = new ChallengeParticipant();
        participant.setUsername(request.getUsername());
        participant.setEmail(request.getEmail());
        participant.setRole(request.getRole());
        boolean removed = challenge.getParticipants().remove(participant);
        if (removed) {
            challengeRepository.save(challenge);
        }
        return removed;
    }

    public List<ChallengeParticipant> getChallengeParticipants(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new RuntimeException("Challenge not found"));
        return List.copyOf(challenge.getParticipants());
    }
}
