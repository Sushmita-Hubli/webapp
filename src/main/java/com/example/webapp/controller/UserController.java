package com.example.webapp.controller;

import com.example.webapp.dto.UserRequestDTO;
import com.example.webapp.dto.UserResponseDTO;
import com.example.webapp.dto.UserUpdateDTO;
import com.example.webapp.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserRequestDTO userRequestDTO
    ) {
        logger.info("POST /v1/user - Creating user with email: {}", userRequestDTO.getEmail());

        UserResponseDTO createdUser = userService.createUser(userRequestDTO);

        logger.info("User created successfully: {}", createdUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/self")
    public ResponseEntity<UserResponseDTO> getUser() {
        logger.info("GET /v1/user/self - Fetching authenticated user info");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        logger.debug("Authenticated user email: {}", email);

        UserResponseDTO user = userService.getUserByEmail(email);

        logger.info("User info retrieved successfully");

        return ResponseEntity.ok(user);
    }

    @PutMapping("/self")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Valid @RequestBody UserUpdateDTO userUpdateDTO
    ) {
        logger.info("PUT /v1/user/self - Updating user info");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        logger.debug("Updating user with email: {}", email);

        UserResponseDTO updatedUser = userService.updateUser(email, userUpdateDTO);

        logger.info("User updated successfully");

        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/self")
    public ResponseEntity<UserResponseDTO> patchUser(
            @Valid @RequestBody UserUpdateDTO userUpdateDTO
    ) {
        logger.info("PATCH /v1/user/self - Updating user info");

        return updateUser(userUpdateDTO);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.debug("Health check requested");
        return ResponseEntity.ok("User API is running");
    }
}

/*
* Explanation of UserController:What is this file?

REST API endpoints for user operations
Handles HTTP requests and responses
Maps URLs to methods
Endpoints created:MethodEndpointAuth Required?PurposePOST/v1/userNOCreate user accountGET/v1/user/selfYESGet user infoPUT/v1/user/selfYESUpdate user infoPATCH/v1/user/selfYESUpdate user infoGET/v1/user/healthNOHealth checkAnnotations explained:
@RestController - Marks this as REST API controller
@RequestMapping("/v1/user") - Base URL for all endpoints
@PostMapping - Handles POST requests
@GetMapping - Handles GET requests
@PutMapping - Handles PUT requests
@PatchMapping - Handles PATCH requests
@Valid - Validates request body
@RequestBody - Converts JSON to DTO
Method 1: createUser()
POST /v1/user
- No authentication required
- Accepts: UserRequestDTO (JSON)
- Returns: 201 Created + UserResponseDTO
- Used to create new accountMethod 2: getUser()
GET /v1/user/self
- Authentication required
- Gets currently logged-in user's info
- Returns: 200 OK + UserResponseDTOMethod 3: updateUser()
PUT /v1/user/self
- Authentication required
- Accepts: UserUpdateDTO (JSON)
- Updates firstName, lastName, password
- Returns: 200 OK + UserResponseDTOMethod 4: patchUser()
PATCH /v1/user/self
- Same as PUT (assignment allows both)
- Just delegates to updateUser()Getting authenticated user:
javaAuthentication authentication = SecurityContextHolder.getContext().getAuthentication();
String email = authentication.getName();

Gets email of logged-in user
Spring Security stores this after authentication*/