package com.example.webapp.service;

import com.example.webapp.dto.UserRequestDTO;
import com.example.webapp.dto.UserResponseDTO;
import com.example.webapp.dto.UserUpdateDTO;
import com.example.webapp.exception.UserAlreadyExistsException;
import com.example.webapp.exception.UserNotFoundException;
import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        logger.info("Creating user with email: {}", userRequestDTO.getEmail());

        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            logger.error("Email already exists: {}", userRequestDTO.getEmail());
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setEmail(userRequestDTO.getEmail());

        String hashedPassword = passwordEncoder.encode(userRequestDTO.getPassword());
        user.setPassword(hashedPassword);

        user.setFirstName(userRequestDTO.getFirstName());
        user.setLastName(userRequestDTO.getLastName());

        User savedUser = userRepository.save(user);

        entityManager.flush();
        entityManager.refresh(savedUser);
        logger.info("User created successfully with ID: {}", savedUser.getId());

        return convertToResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        logger.info("Fetching user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found");
                });

        logger.info("User found: {}", user.getId());

        return convertToResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(String email, UserUpdateDTO userUpdateDTO) {
        logger.info("Updating user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found");
                });

        user.setFirstName(userUpdateDTO.getFirstName());
        user.setLastName(userUpdateDTO.getLastName());

        String hashedPassword = passwordEncoder.encode(userUpdateDTO.getPassword());
        user.setPassword(hashedPassword);

        User updatedUser = userRepository.save(user);

        logger.info("User updated successfully: {}", updatedUser.getId());

        return convertToResponseDTO(updatedUser);
    }

    @Override
    public User loadUserByEmail(String email) {
        logger.debug("Loading user by email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found");
                });
    }

    @Override
    public boolean emailExists(String email) {
        boolean exists = userRepository.existsByEmail(email);
        logger.debug("Email {} exists: {}", email, exists);
        return exists;
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAccountCreated(user.getAccountCreated());
        dto.setAccountUpdated(user.getAccountUpdated());

        logger.debug("Converted user {} to DTO", user.getId());

        return dto;
    }
}

/*
* ### Explanation of UserService:

**What is this file?**
- Contains all business logic for users
- Sits between Controller and Repository
- Handles: validation, password hashing, data transformation

**Why Interface + Implementation?**
- `UserService` = contract (what methods exist)
- `UserServiceImpl` = actual implementation (how they work)
- Makes testing easier

**Methods explained:**

**1. createUser()**
```
Step 1: Check if email exists → if yes, throw exception
Step 2: Create User object
Step 3: Hash password with BCrypt
Step 4: Save to database
Step 5: Convert User to UserResponseDTO
Step 6: Return response
```

**2. getUserByEmail()**
```
Step 1: Find user by email
Step 2: If not found → throw exception
Step 3: Convert to DTO and return
```

**3. updateUser()**
```
Step 1: Find user by email
Step 2: Update firstName, lastName
Step 3: Hash new password
Step 4: Save updated user
Step 5: Convert to DTO and return
```

**4. convertToResponseDTO()**
```
Helper method:
- Converts User entity → UserResponseDTO
- Copies all fields EXCEPT password
- Returns clean response for API
Important annotations:

@Service - Marks this as a service component
@Transactional - All methods run in database transactions
@Autowired - Spring injects dependencies automatically

Logger:

Prints information to console
Helps debug issues
Example: "User created successfully with ID: 123..."*/


