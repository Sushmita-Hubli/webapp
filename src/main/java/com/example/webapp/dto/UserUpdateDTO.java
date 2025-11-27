package com.example.webapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Password is required")
    private String password;
}

/*
* **3. UserUpdateDTO** (for updating user - PUT /v1/user/self)
- Contains: firstName, lastName, password
- NO email (cannot change username)
- NO id, NO timestamps (system managed)*/


/*
*
* **Example Flow:**
```
CLIENT CREATES USER:
Sends → UserRequestDTO {"email": "john@example.com", "password": "secret", ...}
Receives ← UserResponseDTO {"id": "123...", "email": "john@example.com", ...}
(Notice: password NOT in response!)

CLIENT UPDATES USER:
Sends → UserUpdateDTO {"firstName": "Johnny", "lastName": "Doe", "password": "newsecret"}
Receives ← UserResponseDTO {"id": "123...", "firstName": "Johnny", ...}*/