package com.cooknect.user_service.repository;

import com.cooknect.user_service.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel,Long> {
    UserModel findByEmail(String email);
    UserModel findByUsername(String username);
}
