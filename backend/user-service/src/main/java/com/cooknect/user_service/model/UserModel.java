package com.cooknect.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

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

    @Column(name="username",unique = true)
    String username;

    @Column(name="full_name")
    String fullName;

    @Column(name="password")
    String password;


    @Enumerated(EnumType.STRING)
    Role role;

    public enum Role {
        USER, ADMIN
    }

    @ManyToOne
    @JoinColumn(name = "dietary_preference_id")
    private DietaryPreference dietaryPreference;

    @ManyToOne
    @JoinColumn(name = "health_goal_id")
    private HealthGoal healthGoal;

    @ManyToMany
    @JoinTable(
            name = "user_cuisine_preferences",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "cuisine_id")
    )
    private Set<CuisinePreference> cuisinePreferences = new HashSet<>();

}
