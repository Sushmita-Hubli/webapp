package com.example.webapp.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

/*
* **1. Custom Exceptions** (UserAlreadyExistsException, etc.)
- These are special errors for specific problems
- `UserAlreadyExistsException` - thrown when email already exists
- `UserNotFoundException` - thrown when user not found
- `InvalidRequestException` - thrown for bad requests*/