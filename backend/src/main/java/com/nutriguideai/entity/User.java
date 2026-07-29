package com.nutriguideai.entity;

import com.nutriguideai.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String lastName;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    private String gender;
    private Double height;
    private Double weight;
    private String activityLevel;
    private String goal;
    @Enumerated(EnumType.STRING)
    private Role role;
}
