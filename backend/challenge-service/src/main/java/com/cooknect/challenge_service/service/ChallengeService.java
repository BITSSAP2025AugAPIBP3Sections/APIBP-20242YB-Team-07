package com.cooknect.challenge_service.service;

import com.cooknect.challenge_service.dto.CreateChallengeRequest;
import com.cooknect.challenge_service.dto.ChallengeResponse;
import com.cooknect.challenge_service.dto.UpdateChallengeRequest;
import com.cooknect.challenge_service.dto.ChallengeParticipationRequest;
import com.cooknect.challenge_service.dto.RecipeSubmissionRequest;
import com.cooknect.challenge_service.dto.LeaderboardEntry;
import com.cooknect.challenge_service.model.Challenge;
import com.cooknect.challenge_service.model.ChallengeStatus;
import com.cooknect.challenge_service.model.ChallengeParticipant;
import com.cooknect.challenge_service.model.ChallengeRecipeSubmission;
import com.cooknect.challenge_service.repository.ChallengeRepository;
import com.cooknect.challenge_service.repository.ChallengeRecipeSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    @Autowired
    private ChallengeRecipeSubmissionRepository challengeRecipeSubmissionRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${recipe.service.url}")
    private String recipeServiceBaseUrl;
    @Value("${user.service.url}")
    private String userBaseUrl;
    

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
                .anyMatch(p -> Objects.equals(p.getUserId(), request.getUserId()));

        if (alreadyJoined) {
            return false; // Already joined
        }

        // Fetch user details from user-service
        String userServiceUrl = userBaseUrl + request.getUserId();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            userServiceUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> user = response.getBody();
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        ChallengeParticipant participant = new ChallengeParticipant();
        participant.setUserId((Long) user.get("id"));
        participant.setUsername(user.get("username").toString());
        participant.setEmail(user.get("email").toString());
        participant.setRole(user.get("role").toString());
        challenge.getParticipants().add(participant);
        challengeRepository.save(challenge);
        return true;
    }

    public boolean leaveChallenge(Long challengeId, ChallengeParticipationRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new RuntimeException("Challenge not found"));
        ChallengeParticipant participant = new ChallengeParticipant();
        participant.setUserId(request.getUserId());
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

    public boolean submitRecipeToChallenge(Long challengeId, RecipeSubmissionRequest request) {
        // Validate user participation
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new RuntimeException("Challenge not found"));
        boolean isParticipant = challenge.getParticipants().stream()
            .anyMatch(p -> Objects.equals(p.getUserId(), request.getUserId()));
        if (!isParticipant) {
            throw new RuntimeException("User is not a participant in this challenge");
        }
        // Validate recipe existence and ownership via recipe-service (REST)
        String recipeServiceUrl = recipeServiceBaseUrl + "/recipes/id/" + request.getRecipeId();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            recipeServiceUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> recipe = response.getBody();
        if (recipe == null || !Objects.equals(recipe.get("username"), request.getUserName())) {
            throw new RuntimeException("Recipe does not exist or does not belong to user");
        }
        // Prevent duplicate submissions
        boolean alreadySubmitted = !challengeRecipeSubmissionRepository
                .findByChallengeIdAndRecipeId(challengeId, request.getRecipeId()).isEmpty();
        if (alreadySubmitted) {
            throw new RuntimeException("Recipe already submitted to this challenge");
        }
        // Save submission
        ChallengeRecipeSubmission submission = new ChallengeRecipeSubmission();
        submission.setChallengeId(challengeId);
        submission.setRecipeId(request.getRecipeId());
        submission.setUserId(String.valueOf(request.getUserId()));
        submission.setSubmissionTime(LocalDateTime.now());
        challengeRecipeSubmissionRepository.save(submission);
        return true;
    }

    public List<LeaderboardEntry> getChallengeLeaderboard(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new RuntimeException("Challenge not found"));
        List<ChallengeRecipeSubmission> submissions = challengeRecipeSubmissionRepository.findByChallengeId(challengeId);
        Map<String, LeaderboardEntry> leaderboard = new HashMap<>();
        for (ChallengeRecipeSubmission submission : submissions) {
            LeaderboardEntry entry = leaderboard.computeIfAbsent(submission.getUserId(), k -> {
                LeaderboardEntry e = new LeaderboardEntry();
                e.setUserId(submission.getUserId());
                e.setUsername(submission.getUserId()); // You may want to fetch/display username/email
                e.setRecipeCount(0);
                e.setTotalLikes(0);
                return e;
            });
            entry.setRecipeCount(entry.getRecipeCount() + 1);
            // Fetch likes from recipe-service (REST)
            String recipeServiceUrl = recipeServiceBaseUrl + "/recipes/id/" + submission.getRecipeId();
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                recipeServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> recipe = response.getBody();
            if (recipe != null && recipe.get("likes") != null) {
                entry.setTotalLikes(entry.getTotalLikes() + (int) recipe.get("likes"));
            }
        }
        List<LeaderboardEntry> leaderboardList = new ArrayList<>(leaderboard.values());
        // Sort based on challenge type
        if ("MOST_RECIPES".equalsIgnoreCase(String.valueOf(challenge.getType()))) {
            leaderboardList.sort((a, b) -> Integer.compare(b.getRecipeCount(), a.getRecipeCount()));
        } else if ("MOST_LIKES".equalsIgnoreCase(String.valueOf(challenge.getType()))) {
            leaderboardList.sort((a, b) -> Integer.compare(b.getTotalLikes(), a.getTotalLikes()));
        }
        return leaderboardList;
    }
}
