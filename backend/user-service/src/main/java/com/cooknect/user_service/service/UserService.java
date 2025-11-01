package com.cooknect.user_service.service;

import com.cooknect.user_service.dto.UsersDTO;
import com.cooknect.user_service.model.UserModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    public UserModel createUser(UserModel user);

    public String verify(UserModel user);

    public List<UsersDTO> getAllUsers();

    public UsersDTO getUserById(Long id);

    public UsersDTO updateUser(Long id, UsersDTO userDTO, String userEmailHeader);

    public void deleteUser(Long id,String userEmailHeader);
}
