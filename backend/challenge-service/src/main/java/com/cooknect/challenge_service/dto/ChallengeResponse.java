package com.cooknect.challenge_service.dto;

import com.cooknect.challenge_service.model.ChallengeType;
import com.cooknect.challenge_service.model.ChallengeStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ChallengeResponse {
    // Getters and setters
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ChallengeType type;
    private ChallengeStatus status;
    private Boolean isPaid;
    private Integer entryFee;

}
