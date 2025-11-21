package com.cooknect.challenge_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/actuator/health/challenge-service")
    @Operation(summary = "Health Check", description = "", security = {})
    public ResponseEntity<String> challengeServiceHealth() {
        return ResponseEntity.ok("Challenge Service Health");
    }
}
