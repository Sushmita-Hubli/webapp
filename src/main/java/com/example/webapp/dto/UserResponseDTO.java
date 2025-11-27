package com.example.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime accountCreated;
    private LocalDateTime accountUpdated;
}

/*
**2. UserResponseDTO** (for returning user data - GET responses)
- Contains: id, email, firstName, lastName, timestamps
- NO password (security - never return passwords!)
- API returns this when user is created or fetched
*/