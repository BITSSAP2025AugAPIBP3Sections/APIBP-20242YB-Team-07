package com.cooknect.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/actuator/health/user-service")
    @Operation(summary = "Health Check", description = "Authenticate a user", security = {})
    public ResponseEntity<String> userServiceHealth() {
        return ResponseEntity.ok("User Service Health");
    }
}