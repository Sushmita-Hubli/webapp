package com.example.webapp.repository;

import com.example.webapp.model.Product;
import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByOwner(User owner);

    List<Product> findByOwnerEmail(String email);

    long countByOwner(User owner);
}

/*
* Explanation of ProductRepository:What is this?

Handles all database operations for products
Spring generates SQL automatically
Methods explained:
findBySku(String sku)

Finds product by SKU
SQL: SELECT * FROM products WHERE sku = ?
Used to check if SKU exists



existsBySku(String sku)

Checks if SKU exists
SQL: SELECT COUNT(*) > 0 FROM products WHERE sku = ?
Returns true/false



findByOwner(User owner)

Gets all products by a specific user
SQL: SELECT * FROM products WHERE owner_user_id = ?



findByOwnerEmail(String email)

Gets all products by owner's email
SQL: SELECT p.* FROM products p JOIN users u ON p.owner_user_id = u.id WHERE u.email = ?
Spring automatically joins tables!



countByOwner(User owner)

Counts how many products a user has
SQL: SELECT COUNT(*) FROM products WHERE owner_user_id = ?


Inherited methods (FREE):

save(Product product) - Insert or update
findById(UUID id) - Find by ID
findAll() - Get all products
delete(Product product) - Delete product*/