package com.cooknect.user_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="general_queries")
public class GeneralQueries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    Long id;

    @Column(name="name")
    String name;

    @Column(name="email")
    String email;

    @Column(name="subject")
    String subject;

    @Column(name="message")
    String message;
}
