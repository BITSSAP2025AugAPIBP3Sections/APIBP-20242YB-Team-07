package com.cooknect.user_service.controller;

import com.cooknect.user_service.dto.UsersDTO;
import com.cooknect.user_service.model.UserModel;
import com.cooknect.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    UserService service;

    @GetMapping("/hello")
    public void greet(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String userNameHeader = request.getHeader("X-User-Name");
        String userRole = request.getHeader("X-User-Role");

        System.out.println("Authorization Header received in UserService: " + authHeader);
        System.out.println("X-User-Name Header received in UserService: " + userNameHeader);
        System.out.println("X-User-Role Header received in UserService: " + userRole);

    }

    @GetMapping("/")
    @Operation(summary = "Get all users", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<UsersDTO>> getAllUsers() {
        List<UsersDTO> users = service.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsersDTO> getUserById(@PathVariable Long id) {
        UsersDTO users = service.getUserById(id);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}/health-goal")
    @Operation(summary = "Get user health goal", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> getHealthGoal(@PathVariable Long id){
        Map<String, String> health = service.getHealthGoal(id);
        return ResponseEntity.ok(health);
    }

    @GetMapping("/{id}/dietary-preference")
    @Operation(summary = "Get user dietary preference", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> getDietaryPreference(@PathVariable Long id){
        Map<String, String> diet = service.getDietaryPreference(id);
        return ResponseEntity.ok(diet);
    }

    @GetMapping("/{id}/cuisine-preference")
    @Operation(summary = "Get user cuisine preference", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getCuisinePreference(@PathVariable Long id){
        Map<String, Object> cuisine = service.getUserCuisinePreferences(id);
        return ResponseEntity.ok(cuisine);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate a user", security = {})
    public Map<String, String> loginUser(@RequestBody UserModel user){
        String token = service.verify(user);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user", security = {})
    public ResponseEntity<?> createUser(@RequestBody UserModel user) {
        if(user.getPassword() == null || user.getPassword().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Password cannot be empty"));
        }
        if(user.getEmail() == null || user.getEmail().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Email cannot be empty"));
        }
        if(user.getFullName()== null || user.getFullName().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Full name cannot be empty"));
        }
        if(user.getUsername() == null || user.getUsername().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Username cannot be empty"));
        }
        UserModel newuser = service.createUser(user);
        return ResponseEntity.ok(newuser);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UsersDTO userDTO,HttpServletRequest request) {
        String userEmailHeader = request.getHeader("X-User-Email");
        if (userDTO.getEmail() == null || userDTO.getEmail().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Email cannot be empty"));
        }
        if (userDTO.getFullName() == null || userDTO.getFullName().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Full name cannot be empty"));
        }
        if (userDTO.getUsername() == null || userDTO.getUsername().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Username cannot be empty"));
        }
        UsersDTO updatedUser = service.updateUser(id, userDTO,userEmailHeader);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/preferences")
    @Operation(summary = "Update user preferences", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsersDTO> updatePreferences(@PathVariable Long id, UsersDTO userDTO,HttpServletRequest request){
        String userEmailHeader = request.getHeader("X-User-Email");
        UsersDTO updatedUser = service.updatePreferences(id, userDTO, userEmailHeader);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/health-preference")
    @Operation(summary = "Update health preference", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsersDTO> updateHealthGoalPreference(@PathVariable Long id, UsersDTO userDTO,HttpServletRequest request){
        String userEmailHeader = request.getHeader("X-User-Email");
        UsersDTO updatedUser = service.updateHealthGoalPreference(id, userDTO, userEmailHeader);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/dietary-preference")
    @Operation(summary = "Update dietary preference", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsersDTO> updateDietaryPreference(@PathVariable Long id, UsersDTO userDTO,HttpServletRequest request){
        String userEmailHeader = request.getHeader("X-User-Email");
        UsersDTO updatedUser = service.updateDietaryPreference(id, userDTO, userEmailHeader);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/cuisine-preference")
    @Operation(summary = "Update cuisine preference", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsersDTO> updateCuisinePreference(@PathVariable Long id, UsersDTO userDTO,HttpServletRequest request){
        String userEmailHeader = request.getHeader("X-User-Email");
        UsersDTO updatedUser = service.updateCuisinePreference(id, userDTO, userEmailHeader);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,HttpServletRequest request) {
        String userEmailHeader = request.getHeader("X-User-Email");
        service.deleteUser(id,userEmailHeader);
        return ResponseEntity.noContent().build();
    }
}
