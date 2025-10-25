package com.cooknect.user_service.service;

import com.cooknect.user_service.model.UserModel;
import com.cooknect.user_service.model.UserPrincipal;
import com.cooknect.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserModel user = repository.findByEmail(username);

        if(user == null){
            System.out.println("User not found");
            throw new UsernameNotFoundException("User Not Found !!");
        }
        return new UserPrincipal(user);
    }
}
