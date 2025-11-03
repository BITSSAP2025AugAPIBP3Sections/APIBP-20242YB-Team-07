package com.cooknect.nutrition_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyIntakeSummary {
    private double totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
}