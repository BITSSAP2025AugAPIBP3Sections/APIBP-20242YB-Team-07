package com.cooknect.user_service.repository;

import com.cooknect.user_service.model.HealthGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthGoalRepository extends JpaRepository<HealthGoal, Long> {
    Optional<HealthGoal> findByName(String name);
}
