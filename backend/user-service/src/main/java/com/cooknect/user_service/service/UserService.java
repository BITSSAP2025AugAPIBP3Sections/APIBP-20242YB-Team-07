package com.cooknect.user_service.service;

import com.cooknect.user_service.model.UserModel;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public UserModel createUser(UserModel user);

    public String verify(UserModel user);
}
