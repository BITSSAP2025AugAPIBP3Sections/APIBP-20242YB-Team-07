package com.cooknect.user_service.controller;

import com.cooknect.common.events.UserEvent;
import com.cooknect.common.dto.PageRequestDTO;
import com.cooknect.common.dto.PageResponseDTO;
import com.cooknect.user_service.dto.*;
import com.cooknect.user_service.event.UserEventProducer;
import com.cooknect.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    UserService service;

    @Autowired
    UserEventProducer userEventProducer;

    @GetMapping("/")
    @Operation(summary = "Get all users with pagination", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PageResponseDTO<UsersDTO>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        PageRequestDTO pageRequestDTO = new PageRequestDTO();
        pageRequestDTO.setPage(page - 1); // Convert to 0-based for internal processing
        pageRequestDTO.setSize(size);
        pageRequestDTO.setSortBy(sortBy);
        pageRequestDTO.setDirection(direction);
        PageResponseDTO<UsersDTO> users = service.getAllUsers(pageRequestDTO);
        users.setPage(users.getPage() + 1); // Convert back to 1-based for response
        logger.info("All users fetched successfully for page: {}, size: {}", page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsersDTO> getUserById(@PathVariable Long id) {
        UsersDTO users = service.getUserById(id);
        logger.info("User fetched successfully with ID: {}", id);
        return ResponseEntity.ok(users);
    }
    // Bulk fetch usernames by IDs
    @PostMapping("/usernames")
    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"), hidden = true)
    public ResponseEntity<Map<Long, String>> getUsernamesByIds(@RequestBody List<Long> userIds) {
        Map<Long, String> usernames = service.getUsernamesByIds(userIds);
        logger.info("Usernames fetched successfully for IDs: {}", userIds);
        return ResponseEntity.ok(usernames);
    }

    @GetMapping("/user-details")
    @Operation(summary = "Get user details from header", security = @SecurityRequirement(name = "bearerAuth"), hidden = true)
    public ResponseEntity<UsersDTO> getUserDetailsFromHeader(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        Long id;
        try {
            id = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(null);
        }
        UsersDTO users = service.getUserById(id);
        logger.info("User details fetched successfully from header for ID: {}", id);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/health-goal")
    @Operation(summary = "Get user health goal", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> getHealthGoal(@PathVariable Long id){
        Map<String, String> health = service.getHealthGoal(id);
        logger.info("Health goal fetched successfully for user ID: {}", id);
        return ResponseEntity.ok(health);
    }

    @GetMapping("/{id}/dietary-preference")
    @Operation(summary = "Get user dietary preference", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> getDietaryPreference(@PathVariable Long id){
        Map<String, String> diet = service.getDietaryPreference(id);
        logger.info("Dietary preference fetched successfully for user ID: {}", id);
        return ResponseEntity.ok(diet);
    }

    @GetMapping("/{id}/cuisine-preference")
    @Operation(summary = "Get user cuisine preference", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getCuisinePreference(@PathVariable Long id){
        Map<String, Object> cuisine = service.getUserCuisinePreferences(id);
        logger.info("Cuisine preference fetched successfully for user ID: {}", id);
        return ResponseEntity.ok(cuisine);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate a user", security = {})
    public LoginResponseDTO loginUser(@RequestBody LoginRequestDTO loginRequestDTO){
        UsersDTO user = service.getUserByEmail(loginRequestDTO.getEmail());
        UserEvent event = new UserEvent(
        loginRequestDTO.getEmail(),
        "User Logged In Successfully",
        String.format("Hi %s! \nYou have been successfully logged into Cooknect.", user.getFullName())
        );
        userEventProducer.sendUserEvent(event);
        logger.info("User logged in successfully with email: {}", loginRequestDTO.getEmail());
        return service.verify(loginRequestDTO);
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user", security = {})
    public ResponseEntity<?> createUser(@RequestBody CreateUserDTO user) {
        if(user.getPassword() == null || user.getPassword().isEmpty()){
            logger.error("Password cannot be empty during registration");
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot be empty"));
        }
        if(user.getEmail() == null || user.getEmail().isEmpty()){
            logger.error("Email cannot be empty during registration");
            return ResponseEntity.badRequest().body(Map.of("error", "Email cannot be empty"));
        }
        if(user.getFullName()== null || user.getFullName().isEmpty()){
            logger.error("Full name cannot be empty during registration");
            return ResponseEntity.badRequest().body(Map.of("error", "Full name cannot be empty"));
        }
        if(user.getUsername() == null || user.getUsername().isEmpty()){
            logger.error("username cannot be empty during registration");
            return ResponseEntity.badRequest().body(Map.of("error", "Username cannot be empty"));
        }
        CreateUserDTO newUser = service.createUser(user);
        UserEvent event = new UserEvent(
        newUser.getEmail(),
        "User Registered Successfully",
        String.format("Hi %s! \nWelcome to Cooknect. Your account has been created successfully with username %s.", newUser.getFullName(), newUser.getUsername())
        );
        userEventProducer.sendUserEvent(event);
        logger.info("User registered successfully with email: {}", newUser.getEmail());
        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/query")
    @Operation(summary = "Submit a general query", description = "Submit a general query or feedback", security = {})
    public ResponseEntity<GeneralQueriesDTO> submitQuery(@RequestBody GeneralQueriesDTO queryDTO) {
        if(queryDTO.getName() == null || queryDTO.getName().isEmpty()){
            return ResponseEntity.badRequest().body(null);
        }
        if(queryDTO.getEmail() == null || queryDTO.getEmail().isEmpty()){
            return ResponseEntity.badRequest().body(null);
        }
        if(queryDTO.getSubject() == null || queryDTO.getSubject().isEmpty()){
            return ResponseEntity.badRequest().body(null);
        }
        if(queryDTO.getMessage() == null || queryDTO.getMessage().isEmpty()){
            return ResponseEntity.badRequest().body(null);
        }
        GeneralQueriesDTO savedQuery = service.submitGeneralQuery(queryDTO);
        UsersDTO user = service.getUserByEmail(queryDTO.getEmail());
        UserEvent event = new UserEvent(
        queryDTO.getEmail(),
        "Query Submitted Successfully",
        String.format("Hi %s! \nYour query has been submitted successfully on Cooknect.", user.getFullName())
        );
        userEventProducer.sendUserEvent(event);
        logger.info("General query submitted successfully by email: {}", queryDTO.getEmail());
        return ResponseEntity.ok(savedQuery);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UsersDTO userDTO,HttpServletRequest request) {
        String userEmailHeader = request.getHeader("X-User-Email");
        if (userDTO.getEmail() == null || userDTO.getEmail().isEmpty()){
            logger.error("Email cannot be empty during user update");
            return ResponseEntity.badRequest().body(Map.of("error", "Email cannot be empty"));
        }
        if (userDTO.getFullName() == null || userDTO.getFullName().isEmpty()){
            logger.error("Full name cannot be empty during user update");
            return ResponseEntity.badRequest().body(Map.of("error", "Full name cannot be empty"));
        }
        if (userDTO.getUsername() == null || userDTO.getUsername().isEmpty()){
            logger.error("Username cannot be empty during user update");
            return ResponseEntity.badRequest().body(Map.of("error", "Username cannot be empty"));
        }
        if(userDTO.getBio() == null || userDTO.getBio().isEmpty()){
            logger.error("Bio cannot be empty during user update");
            return ResponseEntity.badRequest().body(Map.of("error", "Bio cannot be null"));
        }
        if(userDTO.getAvatarUrl() == null || userDTO.getAvatarUrl().isEmpty()){
            logger.error("Avatar URL cannot be empty during user update");
            return ResponseEntity.badRequest().body(Map.of("error", "Avatar URL cannot be null"));
        }
        if(userDTO.getDietaryPreference() == null || userDTO.getDietaryPreference().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Dietary Preference cannot be null"));
        }
        if(userDTO.getHealthGoal() == null || userDTO.getHealthGoal().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Health Goal cannot be null"));
        }
        if(userDTO.getCuisinePreferences() == null || userDTO.getCuisinePreferences().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Cuisine Preferences cannot be null"));
        }
        UsersDTO updatedUser = service.updateUser(id, userDTO,userEmailHeader);
        UserEvent event = new UserEvent(
        updatedUser.getEmail(),
        "User Updated Successfully",
        String.format("Hi %s! \nYour profile has been successfully updated on Cooknect.", updatedUser.getFullName())
        );
        userEventProducer.sendUserEvent(event);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/preferences")
    @Operation(summary = "Update user preferences", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsersDTO> updatePreferences(@PathVariable Long id, UsersDTO userDTO,HttpServletRequest request){
        String userEmailHeader = request.getHeader("X-User-Email");
        UsersDTO updatedUser = service.updatePreferences(id, userDTO, userEmailHeader);
        UserEvent event = new UserEvent(
        updatedUser.getEmail(),
        "User Preferences Updated Successfully",
        String.format("Hi %s! \nYour preferences have been updated successfully on Cooknect.", updatedUser.getFullName())
        );
        userEventProducer.sendUserEvent(event);
        logger.info("User preferences updated successfully for user ID: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/health-preference")
    @Operation(summary = "Update health preference", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsersDTO> updateHealthGoalPreference(@PathVariable Long id, UsersDTO userDTO,HttpServletRequest request){
        String userEmailHeader = request.getHeader("X-User-Email");
        UsersDTO updatedUser = service.updateHealthGoalPreference(id, userDTO, userEmailHeader);
        UserEvent event = new UserEvent(
        updatedUser.getEmail(),
        "Health Goal Updated Successfully",
        String.format("Hi %s! \nYour health goal has been updated successfully on Cooknect.", updatedUser.getFullName())
        );
        userEventProducer.sendUserEvent(event);
        logger.info("Health goal updated successfully for user ID: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/dietary-preference")
    @Operation(summary = "Update dietary preference", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsersDTO> updateDietaryPreference(@PathVariable Long id, UsersDTO userDTO,HttpServletRequest request){
        String userEmailHeader = request.getHeader("X-User-Email");
        UsersDTO updatedUser = service.updateDietaryPreference(id, userDTO, userEmailHeader);
        UserEvent event = new UserEvent(
        updatedUser.getEmail(),
        "Dietary Preference Updated Successfully",
        String.format("Hi %s! \nYour dietary preference has been updated successfully on Cooknect.", updatedUser.getFullName())
        );
        userEventProducer.sendUserEvent(event);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/cuisine-preference")
    @Operation(summary = "Update cuisine preference", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsersDTO> updateCuisinePreference(@PathVariable Long id, UsersDTO userDTO,HttpServletRequest request){
        String userEmailHeader = request.getHeader("X-User-Email");
        UsersDTO updatedUser = service.updateCuisinePreference(id, userDTO, userEmailHeader);
        UserEvent event = new UserEvent(
        updatedUser.getEmail(),
        "Cuisine Preference Updated Successfully",
        String.format("Hi %s! \nYour cuisine preference has been updated successfully on Cooknect.", updatedUser.getFullName())
        );
        userEventProducer.sendUserEvent(event);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,HttpServletRequest request) {
        String userEmailHeader = request.getHeader("X-User-Email");
        UsersDTO user = service.getUserByEmail(userEmailHeader);
        service.deleteUser(id,userEmailHeader);
        UserEvent event = new UserEvent(
        user.getEmail(),
        "User Deleted Successfully",
        String.format("Hi %s! \nYour account has been deleted successfully from Cooknect.", user.getFullName())
        );
        userEventProducer.sendUserEvent(event);
        logger.info("User deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
