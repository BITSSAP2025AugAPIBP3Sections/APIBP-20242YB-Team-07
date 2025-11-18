package com.cooknect.user_service.service;

import com.cooknect.user_service.dto.*;
import com.cooknect.user_service.model.UserModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface UserService {
    public CreateUserDTO createUser(CreateUserDTO createUserDTO);

    public LoginResponseDTO verify(LoginRequestDTO loginRequestDTO);

    public List<UsersDTO> getAllUsers();

    public UsersDTO getUserById(Long id);

    public UsersDTO updateUser(Long id, UsersDTO userDTO, String userEmailHeader);

    public UsersDTO updateUserGraphql(Long id, UsersDTO userDto);

    public void deleteUser(Long id,String userEmailHeader);

    public UsersDTO updatePreferences(Long id, UsersDTO usersDTO, String userEmailHeader);

    public UsersDTO updateHealthGoalPreference(Long id, UsersDTO usersDTO, String userEmailHeader);

    public UsersDTO updateDietaryPreference(Long id, UsersDTO usersDTO, String userEmailHeader);

    public UsersDTO updateCuisinePreference(Long id, UsersDTO usersDTO, String userEmailHeader);

    public Map<String, String> getDietaryPreference(Long id);

    public Map<String, String> getHealthGoal(Long id);

    public Map<String, Object> getUserCuisinePreferences(Long id);

    public GeneralQueriesDTO submitGeneralQuery(GeneralQueriesDTO generalQueriesDTO);
}
