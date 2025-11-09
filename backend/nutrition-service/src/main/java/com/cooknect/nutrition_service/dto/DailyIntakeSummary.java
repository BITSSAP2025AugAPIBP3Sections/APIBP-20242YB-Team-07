package com.cooknect.nutrition_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyIntakeSummary {
    private double totalFat;
    private double totalSaturatedFat;
    private double totalSodium;
    private double totalPotassium;
    private double totalCholestrol;
    private double totalCarbohydrates;
    private double totalFiber;
    private double totalSugar;
}