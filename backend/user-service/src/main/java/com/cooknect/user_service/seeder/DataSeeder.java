package com.cooknect.user_service.seeder;

import com.cooknect.user_service.model.CuisinePreference;
import com.cooknect.user_service.model.DietaryPreference;
import com.cooknect.user_service.model.HealthGoal;
import com.cooknect.user_service.repository.CuisinePreferenceRepository;
import com.cooknect.user_service.repository.DietaryPreferenceRepository;
import com.cooknect.user_service.repository.HealthGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private DietaryPreferenceRepository dietaryPreferenceRepository;

    @Autowired
    private CuisinePreferenceRepository cuisinePreferenceRepository;

    @Autowired
    private HealthGoalRepository healthGoalRepository;

    @Override public void run(String... args) {
        if (dietaryPreferenceRepository.count() == 0) {
            dietaryPreferenceRepository.saveAll(List.of(
                    new DietaryPreference("Vegan"),
                    new DietaryPreference("Vegetarian"),
                    new DietaryPreference("Keto"),
                    new DietaryPreference("Non-Vegetarian")
            ));
        }

        if (healthGoalRepository.count() == 0) {
            healthGoalRepository.saveAll(List.of(
                    new HealthGoal("Weight Loss"),
                    new HealthGoal("Muscle Gain"),
                    new HealthGoal("Maintain Weight")
            ));
        }

        if (cuisinePreferenceRepository.count() == 0) {
            cuisinePreferenceRepository.saveAll(List.of(
                    new CuisinePreference("Italian"),
                    new CuisinePreference("Indian"),
                    new CuisinePreference("Mexican"),
                    new CuisinePreference("Japanese"),
                    new CuisinePreference("Mediterranean")));
        }
    }
}
