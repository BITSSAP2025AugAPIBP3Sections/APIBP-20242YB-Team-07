package com.cooknect.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UsersDTO {
    private Long id;
    private String email;
    private String role;
    private String username;
    private String fullName;
}
