package com.cooknect.user_service.service;

import com.cooknect.user_service.dto.*;
import com.cooknect.user_service.model.*;
import com.cooknect.user_service.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository repository;

    @Autowired
    DietaryPreferenceRepository dietaryPreferenceRepository;

    @Autowired
    HealthGoalRepository healthGoalRepository;

    @Autowired
    CuisinePreferenceRepository cuisinePreferenceRepository;

    @Autowired
    GeneralQueriesRepository generalQueriesRepository;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    JWTService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public CreateUserDTO createUser(CreateUserDTO user){
        UserModel existingUser = repository.findByEmail(user.getEmail());
        if(existingUser != null){
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
        }
        UserModel existingUsername = repository.findByUsername(user.getUsername());
        if(existingUsername != null){
            throw new RuntimeException("User with username " + user.getUsername() + " already exists");
        }
        UserModel newUser = new UserModel();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(encoder.encode(user.getPassword()));
        newUser.setRole(UserModel.Role.USER);
        newUser.setUsername(user.getUsername());
        newUser.setFullName(user.getFullName());
        UserModel savedUser = repository.save(newUser);
        CreateUserDTO createdUserDTO = new CreateUserDTO();
        createdUserDTO.setEmail(savedUser.getEmail());
        createdUserDTO.setUsername(savedUser.getUsername());
        createdUserDTO.setFullName(savedUser.getFullName());
        createdUserDTO.setPassword(savedUser.getPassword());
        createdUserDTO.setId(savedUser.getId());
        return createdUserDTO;
    }

    @Override
    public LoginResponseDTO verify(LoginRequestDTO loginRequestDTO) {
        try{
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));

            if(authentication.isAuthenticated()){
                UserModel authenticatedUser = repository.findByEmail(loginRequestDTO.getEmail());
                LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
                String token = jwtService.generateToken(loginRequestDTO.getEmail(),authenticatedUser.getRole().name(),authenticatedUser.getId());
                loginResponseDTO.setToken(token);
                loginResponseDTO.setRole(authenticatedUser.getRole().toString());
                return loginResponseDTO;
            }
            else {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
            }
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed due to server error");
        }


    }

    @Override
    public List<UsersDTO> getAllUsers() {
        List<UserModel> users = repository.findAll();
        return users.stream().map(
                u -> new UsersDTO(
                        u.getId(),
                        u.getEmail(),
                        u.getRole().name(),
                        u.getUsername(),
                        u.getFullName(),
                        u.getBio(),
                        u.getAvatarUrl(),
                        u.getDietaryPreference() != null ? u.getDietaryPreference().getName() : null,
                        u.getHealthGoal() != null ? u.getHealthGoal().getName() : null,
                        u.getCuisinePreferences() == null ? List.of() : u.getCuisinePreferences().stream().map(CuisinePreference::getName).toList())).toList();
    }

    @Override
    public UsersDTO getUserById(Long id) {
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return new UsersDTO(user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getUsername(),
                user.getFullName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getDietaryPreference() != null ? user.getDietaryPreference().getName() : null,
                user.getHealthGoal() != null ? user.getHealthGoal().getName() : null,
                user.getCuisinePreferences() == null ? List.of() : user.getCuisinePreferences().stream().map(CuisinePreference::getName).toList());
    }
    @Override
    public Map<Long, String> getUsernamesByIds(List<Long> ids) {
        List<UserModel> users = repository.findAllById(ids); // fetch all users at once
        return users.stream()
                .collect(Collectors.toMap(UserModel::getId, UserModel::getUsername));
    }

    @Override
    public UsersDTO getUserByEmail(String email) {
        UserModel user = repository.findByEmail(email);
        return new UsersDTO(user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getUsername(),
                user.getFullName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getDietaryPreference() != null ? user.getDietaryPreference().getName() : null,
                user.getHealthGoal() != null ? user.getHealthGoal().getName() : null,
                user.getCuisinePreferences() == null ? List.of() : user.getCuisinePreferences().stream().map(CuisinePreference::getName).toList());
    }

    @Override
    public UsersDTO updateUser(Long id, UsersDTO userDTO, String userEmailHeader) {
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if(!user.getEmail().equals(userEmailHeader)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this user");
        }
        if(!userDTO.getEmail().matches(EMAIL_REGEX)){
            throw new RuntimeException("Invalid email format");
        }
        UserModel existingUser = repository.findByEmail(userDTO.getEmail());
        if(existingUser != null && !existingUser.getId().equals(id)){
            throw new RuntimeException("Email already in use by another user");
        }
        UserModel existingUsername = repository.findByUsername(userDTO.getUsername());
        if(existingUsername != null && !existingUsername.getId().equals(id)){
            throw new RuntimeException("Username already in use by another user");
        }
        user.setEmail(userDTO.getEmail());
        user.setUsername(userDTO.getUsername());
        user.setFullName(userDTO.getFullName());
        user.setBio(userDTO.getBio());
        user.setAvatarUrl(userDTO.getAvatarUrl());
        DietaryPreference diet = dietaryPreferenceRepository.findByName(userDTO.getDietaryPreference())
                .orElseThrow(() -> new RuntimeException("Enter a valid Dietary preference"));
        HealthGoal goal = healthGoalRepository.findByName(userDTO.getHealthGoal())
                .orElseThrow(() -> new RuntimeException("Enter a valid Health goal"));
        Set<CuisinePreference> cuisines = userDTO.getCuisinePreferences().stream()
                .map(name -> cuisinePreferenceRepository.findByName(name)
                        .orElseThrow(() -> new RuntimeException("Invalid cuisine: " + name)))
                .collect(Collectors.toSet());
        user.setDietaryPreference(diet);
        user.setHealthGoal(goal);
        user.setCuisinePreferences(cuisines);
        UserModel updatedUser = repository.save(user);
        return new UsersDTO(updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getRole().name(),
                updatedUser.getUsername(),
                updatedUser.getFullName(),
                updatedUser.getBio(),
                updatedUser.getAvatarUrl(),
                updatedUser.getDietaryPreference() != null ? updatedUser.getDietaryPreference().getName() : null,
                updatedUser.getHealthGoal() != null ? updatedUser.getHealthGoal().getName() : null,
                updatedUser.getCuisinePreferences() == null ? List.of() : updatedUser.getCuisinePreferences().stream().map(CuisinePreference::getName).toList()
        );
    }

    @Override
    public UsersDTO updateUserGraphql(Long id, UsersDTO userDto) {
        UserModel existingUser = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));

        if (userDto.getEmail() != null) {
            if (!userDto.getEmail().matches(EMAIL_REGEX))
                throw new RuntimeException("Invalid email format");
            UserModel existingEmail = repository.findByEmail(userDto.getEmail());
            if (existingEmail != null && !existingEmail.getId().equals(id))
                throw new RuntimeException("Email already in use");
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getUsername() != null) {
            UserModel existingUsername = repository.findByUsername(userDto.getUsername());
            if (existingUsername != null && !existingUsername.getId().equals(id))
                throw new RuntimeException("Username already in use");
            existingUser.setUsername(userDto.getUsername());
        }

        if (userDto.getFullName() != null)
            existingUser.setFullName(userDto.getFullName());

        if (userDto.getDietaryPreference() != null) {
            DietaryPreference diet = dietaryPreferenceRepository.findByName(userDto.getDietaryPreference())
                    .orElseThrow(() -> new RuntimeException("Enter a valid Dietary preference"));
            existingUser.setDietaryPreference(diet);
        }

        if (userDto.getHealthGoal() != null) {
            HealthGoal goal = healthGoalRepository.findByName(userDto.getHealthGoal())
                    .orElseThrow(() -> new RuntimeException("Enter a valid Health goal"));
            existingUser.setHealthGoal(goal);
        }

        if (userDto.getCuisinePreferences() != null && !userDto.getCuisinePreferences().isEmpty()) {
            Set<CuisinePreference> cuisines = userDto.getCuisinePreferences().stream()
                    .map(name -> cuisinePreferenceRepository.findByName(name)
                            .orElseThrow(() -> new RuntimeException("Invalid cuisine: " + name)))
                    .collect(Collectors.toSet());
            existingUser.setCuisinePreferences(cuisines);
        }

        UserModel updatedUser = repository.save(existingUser);
        return new UsersDTO(updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getRole().name(),
                updatedUser.getUsername(),
                updatedUser.getFullName(),
                updatedUser.getBio(),
                updatedUser.getAvatarUrl(),
                updatedUser.getDietaryPreference() != null ? updatedUser.getDietaryPreference().getName() : null,
                updatedUser.getHealthGoal() != null ? updatedUser.getHealthGoal().getName() : null,
                updatedUser.getCuisinePreferences() == null ? List.of() : updatedUser.getCuisinePreferences().stream().map(CuisinePreference::getName).toList()
        );
    }



    @Override
    public void deleteUser(Long id, String userEmailHeader) {
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if(!user.getEmail().equals(userEmailHeader)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to delete this user");
        }
        repository.deleteById(id);
    }

    @Override
    public UsersDTO updatePreferences(Long id, UsersDTO usersDTO, String userEmailHeader) {
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getEmail().equals(userEmailHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this user");
        }

        DietaryPreference diet = dietaryPreferenceRepository.findByName(usersDTO.getDietaryPreference()).orElseThrow(() -> new RuntimeException("Enter a valid Dietary preference"));
        HealthGoal goal = healthGoalRepository.findByName(usersDTO.getHealthGoal()).orElseThrow(() -> new RuntimeException("Enter a valid Health goal"));
        Set<CuisinePreference> cuisines = usersDTO.getCuisinePreferences().stream()
                .map(name -> cuisinePreferenceRepository.findByName(name)
                        .orElseThrow(() -> new RuntimeException("Invalid cuisine: " + name)))
                .collect(Collectors.toSet());

        user.setDietaryPreference(diet);
        user.setHealthGoal(goal);
        user.setCuisinePreferences(cuisines);

        UserModel updatedUser = repository.save(user);
        return new UsersDTO(updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getRole().name(),
                updatedUser.getUsername(),
                updatedUser.getFullName(),
                updatedUser.getBio(),
                updatedUser.getAvatarUrl(),
                updatedUser.getDietaryPreference() != null ? updatedUser.getDietaryPreference().getName() : null,
                updatedUser.getHealthGoal() != null ? updatedUser.getHealthGoal().getName() : null,
                updatedUser.getCuisinePreferences() == null ? List.of() : updatedUser.getCuisinePreferences().stream().map(CuisinePreference::getName).toList()
        );
    }

    @Override
    public UsersDTO updateHealthGoalPreference(Long id, UsersDTO usersDTO, String userEmailHeader) {
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getEmail().equals(userEmailHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this user");
        }
        HealthGoal goal = healthGoalRepository.findByName(usersDTO.getHealthGoal()).orElseThrow(() -> new RuntimeException("Enter a valid Health goal"));
        user.setHealthGoal(goal);

        UserModel updatedUser = repository.save(user);
        return new UsersDTO(updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getRole().name(),
                updatedUser.getUsername(),
                updatedUser.getFullName(),
                updatedUser.getBio(),
                updatedUser.getAvatarUrl(),
                updatedUser.getDietaryPreference() != null ? updatedUser.getDietaryPreference().getName() : null,
                updatedUser.getHealthGoal() != null ? updatedUser.getHealthGoal().getName() : null,
                updatedUser.getCuisinePreferences() == null ? List.of() : updatedUser.getCuisinePreferences().stream().map(CuisinePreference::getName).toList()
        );
    }

    @Override
    public UsersDTO updateDietaryPreference(Long id, UsersDTO usersDTO, String userEmailHeader) {
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getEmail().equals(userEmailHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this user");
        }
        DietaryPreference diet = dietaryPreferenceRepository.findByName(usersDTO.getHealthGoal()).orElseThrow(() -> new RuntimeException("Enter a valid Health goal"));
        user.setDietaryPreference(diet);

        UserModel updatedUser = repository.save(user);
        return new UsersDTO(updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getRole().name(),
                updatedUser.getUsername(),
                updatedUser.getFullName(),
                updatedUser.getBio(),
                updatedUser.getAvatarUrl(),
                updatedUser.getDietaryPreference() != null ? updatedUser.getDietaryPreference().getName() : null,
                updatedUser.getHealthGoal() != null ? updatedUser.getHealthGoal().getName() : null,
                updatedUser.getCuisinePreferences() == null ? List.of() : updatedUser.getCuisinePreferences().stream().map(CuisinePreference::getName).toList()
        );
    }

    @Override
    public UsersDTO updateCuisinePreference(Long id, UsersDTO usersDTO, String userEmailHeader) {
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getEmail().equals(userEmailHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized to update this user");
        }
        Set<CuisinePreference> cuisines = usersDTO.getCuisinePreferences().stream()
                .map(name -> cuisinePreferenceRepository.findByName(name)
                        .orElseThrow(() -> new RuntimeException("Invalid cuisine: " + name)))
                .collect(Collectors.toSet());
        user.setCuisinePreferences(cuisines);

        UserModel updatedUser = repository.save(user);
        return new UsersDTO(updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getRole().name(),
                updatedUser.getUsername(),
                updatedUser.getFullName(),
                updatedUser.getBio(),
                updatedUser.getAvatarUrl(),
                updatedUser.getDietaryPreference() != null ? updatedUser.getDietaryPreference().getName() : null,
                updatedUser.getHealthGoal() != null ? updatedUser.getHealthGoal().getName() : null,
                updatedUser.getCuisinePreferences() == null ? List.of() : updatedUser.getCuisinePreferences().stream().map(CuisinePreference::getName).toList()
        );
    }

    @Override
    public Map<String, String> getDietaryPreference(Long id){
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getDietaryPreference() == null) {
            return Map.of("message", "User has not updated their dietary preference.");
        }
        return Map.of("diet", user.getDietaryPreference().getName());
    }

    @Override
    public Map<String, String> getHealthGoal(Long id){
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getHealthGoal() == null) {
            return Map.of("message", "User has not updated their health goal.");
        }
        return Map.of("goal", user.getDietaryPreference().getName());
    }

    @Override
    public Map<String, Object> getUserCuisinePreferences(Long id){
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        Set<CuisinePreference> cuisineSet = user.getCuisinePreferences();
        List<String> cuisines = (cuisineSet != null)
                ? cuisineSet.stream()
                .map(CuisinePreference::getName)
                .toList()
                : List.of();
        if (cuisines.isEmpty()) {
            return Map.of(
                    "message", "User has not added any cuisine preferences.",
                    "cuisinePreferences", cuisines
            );
        }

        return Map.of("cuisinePreferences", cuisines);
    }

    @Override
    public GeneralQueriesDTO submitGeneralQuery(GeneralQueriesDTO generalQueriesDTO){
        GeneralQueries generalQueries = new GeneralQueries();
        generalQueries.setName(generalQueriesDTO.getName());
        generalQueries.setEmail(generalQueriesDTO.getEmail());
        generalQueries.setSubject(generalQueriesDTO.getSubject());
        generalQueries.setMessage(generalQueriesDTO.getMessage());
        GeneralQueries savedQuery = generalQueriesRepository.save(generalQueries);
        GeneralQueriesDTO savedQueryDTO = new GeneralQueriesDTO();
        savedQueryDTO.setName(savedQuery.getName());
        savedQueryDTO.setEmail(savedQuery.getEmail());
        savedQueryDTO.setSubject(savedQuery.getSubject());
        savedQueryDTO.setMessage(savedQuery.getMessage());
        return savedQueryDTO;
    }
}
