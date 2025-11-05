package com.cooknect.challenge_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaderboardEntry {
    private String userId;
    private String username;
    private int recipeCount;
    private int totalLikes;
}
