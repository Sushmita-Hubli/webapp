package com.example.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponseDTO(LocalDateTime timestamp, int status, String error, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = null;
    }
}

/*
* **2. ErrorResponseDTO**
- Standard format for all error responses
- Contains: timestamp, status code, error name, message, path
- Every error returns the same structure*/

/*
*
* ### Explanation of Exception Handling:

**What are these exception files?**

**1. Custom Exceptions** (UserAlreadyExistsException, etc.)
- These are special errors for specific problems
- `UserAlreadyExistsException` - thrown when email already exists
- `UserNotFoundException` - thrown when user not found
- `InvalidRequestException` - thrown for bad requests

**2. ErrorResponseDTO**
- Standard format for all error responses
- Contains: timestamp, status code, error name, message, path
- Every error returns the same structure

**3. GlobalExceptionHandler**
- Catches ALL exceptions in your application
- Converts exceptions into proper error responses
- Returns appropriate HTTP status codes

**How it works:**
```
1. Something goes wrong in your code
2. Exception is thrown (e.g., UserAlreadyExistsException)
3. GlobalExceptionHandler catches it
4. Converts to ErrorResponseDTO
5. Returns to client with proper status code
```

**Example:**
```
If email already exists:
Throws → UserAlreadyExistsException
Handler catches it
Returns → 400 Bad Request
{
  "timestamp": "2024-11-26T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already exists",
  "path": "/v1/user"
}*/