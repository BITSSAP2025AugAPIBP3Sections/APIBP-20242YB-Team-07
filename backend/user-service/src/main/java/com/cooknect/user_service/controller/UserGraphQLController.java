package com.cooknect.user_service.controller;

import com.cooknect.common.dto.PageRequestDTO;
import com.cooknect.common.dto.PageResponseDTO;
import com.cooknect.user_service.dto.CreateUserDTO;
import com.cooknect.user_service.dto.UsersDTO;
import com.cooknect.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/graphql")
public class UserGraphQLController {

    @Autowired
    private UserService userService;

    @QueryMapping
    public PageResponseDTO<UsersDTO> getAllUsers(@Argument Integer page, 
                                                 @Argument Integer size,
                                                 @Argument String sortBy,
                                                 @Argument String direction) {
        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPage(page != null ? page - 1 : 0); // Convert to 0-based for internal processing
        pageRequest.setSize(size != null ? size : 10);
        pageRequest.setSortBy(sortBy != null ? sortBy : "id");
        pageRequest.setDirection(direction != null ? direction : "asc");
        PageResponseDTO<UsersDTO> result = userService.getAllUsers(pageRequest);
        result.setPage(result.getPage() + 1); // Convert back to 1-based for response
        return result;
    }

    @QueryMapping
    public UsersDTO getUserById(@Argument Long userId) {
        return userService.getUserById(userId);
    }

    @MutationMapping
    public CreateUserDTO registerUser(@Argument CreateUserDTO user) {
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

        com.cooknect.user_service.dto.CreateUserDTO newUser = userService.createUser(user);
        return newUser;
    }

    @MutationMapping
    public UsersDTO updateUser(@Argument Long userId, @Argument("user") UsersDTO userDto) {
        return userService.updateUserGraphql(userId, userDto);
    }
}
