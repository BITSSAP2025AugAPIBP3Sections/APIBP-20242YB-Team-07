package com.cooknect.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="users")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    Long id;


    @Column(name="email",unique = true)
    String email;


    @Column(name="password")
    String password;


    @Enumerated(EnumType.STRING)
    Role role;

    public enum Role {
        USER, ADMIN
    }
}
