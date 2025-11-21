package com.cooknect.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeEvent {
    private String email;
    private String messageSubject;
    private String messageBody;
}
