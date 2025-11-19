package com.cooknect.user_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UsersDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    private String email;
    private String role;
    private String username;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private String dietaryPreference;
    private String healthGoal;
    private List<String> cuisinePreferences;
}
