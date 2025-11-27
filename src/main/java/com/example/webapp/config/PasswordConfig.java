package com.example.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

/*
*
* Explanation of PasswordConfig:What is this file?

Configures BCrypt password encoder
BCrypt is used to hash passwords securely
What is password hashing?Plain text password: "MySecret123"
After BCrypt hashing: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."

- Cannot reverse the hash to get password back
- Same password creates different hashes (uses salt)
- Takes time to hash (prevents brute force attacks)Why BCrypt?

Industry standard
Very secure
Assignment requirement: "BCrypt password hashing scheme with salt"
@Bean annotation:

Makes this object available throughout the application
Anywhere you need to hash/verify passwords, Spring injects this automatically
How it's used:java// Hash password when creating user
String plainPassword = "MySecret123";
String hashedPassword = passwordEncoder.encode(plainPassword);
// Store hashedPassword in database

// Verify password when logging in
String inputPassword = "MySecret123";
String storedHash = "$2a$10$N9qo..."; // from database
boolean matches = passwordEncoder.matches(inputPassword, storedHash);
// If matches = true, password is correct*/