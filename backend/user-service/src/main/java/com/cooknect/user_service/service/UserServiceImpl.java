package com.cooknect.user_service.service;

import com.cooknect.user_service.model.UserModel;
import com.cooknect.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository repository;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    JWTService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public UserModel createUser(UserModel user){
        UserModel newUser = new UserModel();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(encoder.encode(user.getPassword()));
        newUser.setRole(UserModel.Role.USER);
        return repository.save(newUser);
    }

    @Override
    public String verify(UserModel user) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPassword()));

        if(authentication.isAuthenticated()){
            System.out.println("User verified");
            return jwtService.generateToken(user.getEmail());
        }
        return "Fail";
    }
}
