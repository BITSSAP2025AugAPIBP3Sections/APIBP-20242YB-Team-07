package com.cooknect.user_service.repository;

import com.cooknect.user_service.model.CuisinePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CuisinePreferenceRepository extends JpaRepository<CuisinePreference,Long> {
    Optional<CuisinePreference> findByName(String name);
}
