package com.example.webapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;
}


/*### Explanation of DTOs:

**What are DTOs?**
- DTOs = Data Transfer Objects
- They control what data goes IN and OUT of your API

**Why do we need 3 different DTOs?**

**1. UserRequestDTO** (for creating user - POST /v1/user)
- Contains: email, password, firstName, lastName
- NO id, NO timestamps (system generates these)
- Users send this when creating account*/