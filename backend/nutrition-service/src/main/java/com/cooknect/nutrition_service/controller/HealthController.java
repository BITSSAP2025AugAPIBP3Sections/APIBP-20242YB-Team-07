package com.cooknect.nutrition_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/actuator/health/nutrition-service")
    @Operation(summary = "Health Check", description = "", security = {})
    public ResponseEntity<String> nutritionServiceHealth() {
        return ResponseEntity.ok("Nutrition Service Health");
    }
}
