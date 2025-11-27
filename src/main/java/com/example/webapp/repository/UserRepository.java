package com.example.webapp.repository;

import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}


/*
Explanation of UserRepository:
What is this file?

This handles all database operations for User
You don't write SQL - Spring generates it automatically

Why is it an interface, not a class?

Spring Data JPA creates the implementation automatically at runtime
You just define method names, Spring figures out what SQL to run

Methods explained:

findByEmail(String email)

Finds user by email address
Returns Optional<User> - either has user or is empty
Spring generates: SELECT * FROM users WHERE email = ?


existsByEmail(String email)

Checks if email exists in database
Returns true/false
Spring generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?



Inherited methods (you get these for FREE):

save(User user) - Insert or update
findById(UUID id) - Find by ID
findAll() - Get all users
delete(User user) - Delete user
count() - Count users
*/