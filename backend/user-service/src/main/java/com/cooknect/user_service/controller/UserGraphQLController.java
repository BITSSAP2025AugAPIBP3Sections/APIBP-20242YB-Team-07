package com.cooknect.user_service.controller;

import com.cooknect.user_service.dto.UsersDTO;
import com.cooknect.user_service.model.UserModel;
import com.cooknect.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/graphql")
public class UserGraphQLController {

    @Autowired
    private UserService userService;

    @QueryMapping
    public List<UsersDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @QueryMapping
    public UsersDTO getUserById(@Argument Long id) {
        return userService.getUserById(id);
    }

    @MutationMapping
    public UserModel registerUser(@Argument UserModel user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (user.getFullName() == null || user.getFullName().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        UserModel newUser = userService.createUser(user);
        return newUser;
    }

    @MutationMapping
    public UserModel updateUser(@Argument Long id, @Argument UserModel user) {
        return userService.updateUserGraphql(id, user);
    }
}
