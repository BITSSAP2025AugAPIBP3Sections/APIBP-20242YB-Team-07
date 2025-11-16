package com.cooknect.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeneralQueriesDTO {
    String name;
    String email;
    String subject;
    String message;
}
