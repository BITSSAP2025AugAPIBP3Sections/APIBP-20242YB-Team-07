package com.cooknect.user_service.service;

import com.cooknect.user_service.dto.UsersDTO;
import com.cooknect.user_service.model.UserModel;
import com.cooknect.user_service.repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository repository;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    JWTService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Override
    public UserModel createUser(UserModel user){
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
        return repository.save(newUser);
    }

    @Override
    public String verify(UserModel user) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPassword()));

        if(authentication.isAuthenticated()){
            UserModel authenticatedUser = repository.findByEmail(user.getEmail());
            return jwtService.generateToken(user.getEmail(),authenticatedUser.getRole().name(),authenticatedUser.getUsername());
        }
        return "Fail";
    }

    @Override
    public List<UsersDTO> getAllUsers() {
        List<UserModel> users = repository.findAll();
        return users.stream().map(u -> new UsersDTO(u.getId(), u.getEmail(), u.getRole().name(), u.getUsername(),u.getFullName())).toList();
    }

    @Override
    public UsersDTO getUserById(Long id) {
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return new UsersDTO(user.getId(), user.getEmail(), user.getRole().name(), user.getUsername(),user.getFullName());
    }

    @Override
    public UsersDTO updateUser(Long id, UsersDTO userDTO, String userEmailHeader) {
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if(!user.getEmail().equals(userEmailHeader)){
            throw new RuntimeException("Unauthorized to update this user");
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
        UserModel updatedUser = repository.save(user);
        return new UsersDTO(updatedUser.getId(), updatedUser.getEmail(), updatedUser.getRole().name(), updatedUser.getUsername(),updatedUser.getFullName());
    }

    @Override
    public void deleteUser(Long id, String userEmailHeader) {
        UserModel user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if(!user.getEmail().equals(userEmailHeader)){
            throw new RuntimeException("Unauthorized to delete this user");
        }
        repository.deleteById(id);
    }
}
