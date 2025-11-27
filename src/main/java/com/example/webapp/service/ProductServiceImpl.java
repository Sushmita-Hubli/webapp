package com.example.webapp.service;

import com.example.webapp.dto.ProductRequestDTO;
import com.example.webapp.dto.ProductResponseDTO;
import com.example.webapp.dto.ProductUpdateDTO;
import com.example.webapp.exception.ProductAlreadyExistsException;
import com.example.webapp.exception.ProductNotFoundException;
import com.example.webapp.exception.UnauthorizedAccessException;
import com.example.webapp.exception.UserNotFoundException;
import com.example.webapp.model.Product;
import com.example.webapp.model.User;
import com.example.webapp.repository.ProductRepository;
import com.example.webapp.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO, String ownerEmail) {
        logger.info("Creating product with SKU: {} for user: {}", productRequestDTO.getSku(), ownerEmail);

        if (productRepository.existsBySku(productRequestDTO.getSku())) {
            logger.error("SKU already exists: {}", productRequestDTO.getSku());
            throw new ProductAlreadyExistsException("Product with SKU " + productRequestDTO.getSku() + " already exists");
        }

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> {
                    logger.error("Owner not found with email: {}", ownerEmail);
                    return new UserNotFoundException("User not found");
                });

        logger.debug("Owner found: {} {}", owner.getFirstName(), owner.getLastName());

        Product product = new Product();
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setSku(productRequestDTO.getSku());
        product.setManufacturer(productRequestDTO.getManufacturer());
        product.setQuantity(productRequestDTO.getQuantity());
        product.setOwner(owner);

        logger.debug("Product entity created, saving to database");

        Product savedProduct = productRepository.save(product);
        entityManager.flush();
        entityManager.refresh(savedProduct);

        logger.info("Product created successfully with ID: {}", savedProduct.getId());

        return convertToResponseDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO getProductById(UUID id) {
        logger.info("Fetching product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", id);
                    return new ProductNotFoundException("Product not found with ID: " + id);
                });

        logger.info("Product found: {}", product.getName());

        return convertToResponseDTO(product);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        logger.info("Fetching all products");

        List<Product> products = productRepository.findAll();

        logger.info("Found {} products", products.size());

        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getProductsByOwner(String ownerEmail) {
        logger.info("Fetching products for owner: {}", ownerEmail);

        List<Product> products = productRepository.findByOwnerEmail(ownerEmail);

        logger.info("Found {} products for owner {}", products.size(), ownerEmail);

        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO updateProduct(UUID id, ProductUpdateDTO productUpdateDTO, String authenticatedEmail) {
        logger.info("Updating product ID: {} by user: {}", id, authenticatedEmail);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", id);
                    return new ProductNotFoundException("Product not found with ID: " + id);
                });

        logger.debug("Product found: {}", product.getName());

        if (!isOwner(product, authenticatedEmail)) {
            logger.error("User {} attempted to update product {} owned by {}",
                    authenticatedEmail, id, product.getOwner().getEmail());
            throw new UnauthorizedAccessException("You are not authorized to update this product");
        }

        logger.debug("Ownership verified");

        String newSku = productUpdateDTO.getSku();
        String currentSku = product.getSku();

        if (!newSku.equals(currentSku)) {
            logger.debug("SKU is changing from {} to {}", currentSku, newSku);

            if (productRepository.existsBySku(newSku)) {
                logger.error("Cannot update: SKU {} already exists", newSku);
                throw new ProductAlreadyExistsException("Product with SKU " + newSku + " already exists");
            }
        }

        product.setName(productUpdateDTO.getName());
        product.setDescription(productUpdateDTO.getDescription());
        product.setSku(productUpdateDTO.getSku());
        product.setManufacturer(productUpdateDTO.getManufacturer());
        product.setQuantity(productUpdateDTO.getQuantity());

        logger.debug("Product fields updated");

        Product updatedProduct = productRepository.save(product);
        entityManager.flush();
        entityManager.refresh(updatedProduct);

        logger.info("Product updated successfully: {}", updatedProduct.getId());

        return convertToResponseDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(UUID id, String authenticatedEmail) {
        logger.info("Deleting product ID: {} by user: {}", id, authenticatedEmail);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", id);
                    return new ProductNotFoundException("Product not found with ID: " + id);
                });

        logger.debug("Product found: {}", product.getName());

        if (!isOwner(product, authenticatedEmail)) {
            logger.error("User {} attempted to delete product {} owned by {}",
                    authenticatedEmail, id, product.getOwner().getEmail());
            throw new UnauthorizedAccessException("You are not authorized to delete this product");
        }

        logger.debug("Ownership verified");

        productRepository.delete(product);

        logger.info("Product deleted successfully: {}", id);
    }

    @Override
    public boolean isOwner(Product product, String userEmail) {
        String ownerEmail = product.getOwner().getEmail();
        boolean isOwner = ownerEmail.equals(userEmail);

        logger.debug("Ownership check: product owner={}, user={}, isOwner={}",
                ownerEmail, userEmail, isOwner);

        return isOwner;
    }

    private ProductResponseDTO convertToResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setSku(product.getSku());
        dto.setManufacturer(product.getManufacturer());
        dto.setQuantity(product.getQuantity());
        dto.setDateAdded(product.getDateAdded());
        dto.setDateLastUpdated(product.getDateLastUpdated());
        dto.setOwnerUserId(product.getOwner().getId());

        logger.debug("Converted product {} to DTO", product.getId());

        return dto;
    }
}

/*
* ### Explanation of ProductService:

**What this service does:**

**1. createProduct()** - Creates new product
```
- Check if SKU exists (must be unique)
- Find owner user by email
- Create product entity
- Set all fields including owner
- Save to database with timestamps
- Return response DTO
```

**2. getProductById()** - Get single product
```
- Find product by ID
- If not found → throw exception (404)
- Convert to DTO and return
```

**3. getAllProducts()** - Get all products
```
- Get all products from database
- Convert each to DTO
- Return list
```

**4. getProductsByOwner()** - Get user's products
```
- Find all products by owner email
- Convert to DTOs
- Return list
```

**5. updateProduct()** - Update product
```
- Find product by ID
- Check if user is owner (IMPORTANT!)
- If not owner → throw UnauthorizedAccessException (403)
- If SKU changed, check it's unique
- Update all fields
- Save and return
```

**6. deleteProduct()** - Delete product
```
- Find product by ID
- Check if user is owner (IMPORTANT!)
- If not owner → throw UnauthorizedAccessException (403)
- Delete product
```

**7. isOwner()** - Check ownership
```
- Compare product owner's email with user's email
- Return true/false
- Used by update and delete methods*/