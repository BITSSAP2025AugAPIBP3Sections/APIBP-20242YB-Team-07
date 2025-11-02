package com.cooknect.challenge_service.dto;

import com.cooknect.challenge_service.model.ChallengeType;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
public class UpdateChallengeRequest {
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ChallengeType type;
}
