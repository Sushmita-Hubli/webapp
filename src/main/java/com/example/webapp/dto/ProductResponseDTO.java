package com.example.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private String sku;
    private String manufacturer;
    private Integer quantity;
    private LocalDateTime dateAdded;
    private LocalDateTime dateLastUpdated;
    private UUID ownerUserId;
}


/*
*### Explanation of Product DTOs:

**Three DTOs for different purposes:**

**1. ProductRequestDTO** (Creating product - POST)
- What user sends when creating product
- Contains: name, description, sku, manufacturer, quantity
- NO id, NO timestamps, NO owner (system sets these)

**2. ProductUpdateDTO** (Updating product - PUT/PATCH)
- What user sends when updating product
- Same fields as ProductRequestDTO
- All product fields can be updated
- Assignment: "Only owner can update"

**3. ProductResponseDTO** (API response - GET/POST/PUT)
- What API returns
- Contains everything including id, timestamps, ownerUserId
- Shows who owns the product (ownerUserId)

**Example flow:**
```
CREATE PRODUCT:
Client sends → ProductRequestDTO
{
  "name": "Laptop",
  "sku": "LAP-001",
  "manufacturer": "Dell",
  "quantity": 50
}

Server returns → ProductResponseDTO
{
  "id": "123...",
  "name": "Laptop",
  "sku": "LAP-001",
  "manufacturer": "Dell",
  "quantity": 50,
  "dateAdded": "2024-11-26T11:00:00",
  "dateLastUpdated": "2024-11-26T11:00:00",
  "ownerUserId": "999..."  ← Who created it
}
* */