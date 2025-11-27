package com.example.webapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotBlank(message = "SKU is required")
    @Column(unique = true, nullable = false)
    private String sku;

    @NotBlank(message = "Manufacturer is required")
    @Column(nullable = false)
    private String manufacturer;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be less than 0")
    @Column(nullable = false)
    private Integer quantity;

    @CreationTimestamp
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "date_added", nullable = false, updatable = false)
    private LocalDateTime dateAdded;

    @UpdateTimestamp
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "date_last_updated", nullable = false)
    private LocalDateTime dateLastUpdated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User owner;
}

/*
*
* ### Explanation of Product Entity:

**What is this?**
- Represents a product in the database
- Each Product object = one row in `products` table

**Fields:**

1. **id** - Auto-generated UUID (primary key)
2. **name** - Product name (required)
3. **description** - Product description (optional, up to 1000 characters)
4. **sku** - Stock Keeping Unit (required, unique)
5. **manufacturer** - Company that makes it (required)
6. **quantity** - How many in stock (required, minimum 0)
7. **dateAdded** - When product was created (auto-generated)
8. **dateLastUpdated** - When product was last modified (auto-updated)
9. **owner** - User who created the product (relationship)

**Important validations:**

- `@Min(value = 0)` on quantity - Assignment requirement: "Product quantity cannot be less than 0"
- `@Column(unique = true)` on sku - Each product must have unique SKU
- `@NotBlank` - Field cannot be empty

**Relationship with User:**
```
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "owner_user_id")
private User owner;
What this means:

Many products can belong to one user
Creates foreign key owner_user_id in products table
Links to id in users table
Used to check: "Does this user own this product?"

Database table that will be created:
sqlCREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    sku VARCHAR(255) UNIQUE NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    date_added DATETIME NOT NULL,
    date_last_updated DATETIME NOT NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    FOREIGN KEY (owner_user_id) REFERENCES users(id)
);*/