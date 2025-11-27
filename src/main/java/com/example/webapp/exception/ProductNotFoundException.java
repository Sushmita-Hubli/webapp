package com.example.webapp.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

/*
* Explanation of Product Exceptions:
Three custom exceptions for products:
1. ProductAlreadyExistsException

Thrown when: Creating/updating product with existing SKU
Example: User tries to create product with SKU "LAP-001" but it already exists
Returns: 400 Bad Request

2. ProductNotFoundException

Thrown when: Product with given ID doesn't exist
Example: User tries to get/update/delete product ID "999" but it doesn't exist
Returns: 404 Not Found

3. UnauthorizedAccessException

Thrown when: User tries to update/delete someone else's product
Example: User A tries to delete product owned by User B
Returns: 403 Forbidden
Assignment requirement: "Only the user who added the product can update/delete"

Difference between 401 and 403:

401 Unauthorized - Not logged in (no/wrong credentials)
403 Forbidden - Logged in but no permission (not the owner)*/