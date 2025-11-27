package com.example.webapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @CreationTimestamp
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "account_created", nullable = false, updatable = false)
    private LocalDateTime accountCreated;

    @UpdateTimestamp
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "account_updated", nullable = false)
    private LocalDateTime accountUpdated;
}


/*
Explanation of User Entity:
What is this file?

This represents a user in your database
Each User object = one row in users table

Important annotations:

@Entity - Tells Spring this is a database table
@Table(name = "users") - Table name in MySQL
@Data - Lombok generates getters/setters automatically
@Id - Primary key
@GeneratedValue - Auto-generate UUID
@Column(unique = true) - Email must be unique
@JsonProperty(access = WRITE_ONLY) - Password never returned in responses
@JsonProperty(access = READ_ONLY) - Timestamps cannot be set by users
@CreationTimestamp - Set automatically when created
@UpdateTimestamp - Updated automatically when modified

What database table will be created?
sqlCREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    account_created DATETIME NOT NULL,
    account_updated DATETIME NOT NULL
);
 */