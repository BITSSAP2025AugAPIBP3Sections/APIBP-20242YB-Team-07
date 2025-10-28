package com.cooknect.user_service.controller;

import com.cooknect.user_service.model.UserModel;
import com.cooknect.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    UserService service;

    @GetMapping("/")
    public String greet(HttpServletRequest request) {
        return "Welcome to User MicroService : "+request.getSession().getId();
    }

    @PostMapping("/register")
    public ResponseEntity<UserModel> createUser(@RequestBody UserModel user) {
        UserModel newuser = service.createUser(user);
        return ResponseEntity.ok(newuser);
    }

    @PostMapping("/login")
    public Map<String, String> loginUser(@RequestBody UserModel user){
        String token = service.verify(user);
//        String role = service.getRoleByEmail(user.getEmail());
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
//        response.put("role", role);
        return response;
    }
}
