package com.cooknect.user_service.repository;

import com.cooknect.user_service.model.DietaryPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DietaryPreferenceRepository extends JpaRepository<DietaryPreference,Long> {
    Optional<DietaryPreference> findByName(String name);
}
