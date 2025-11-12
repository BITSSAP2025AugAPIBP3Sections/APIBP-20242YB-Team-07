package com.cooknect.challenge_service.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChallengeParticipationRequest {
    private Long userId;
    private String username;
    private String email;
    private String role;
}
