package com.example.webapp.controller;

import com.example.webapp.dto.ProductRequestDTO;
import com.example.webapp.dto.ProductResponseDTO;
import com.example.webapp.dto.ProductUpdateDTO;
import com.example.webapp.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/product")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductRequestDTO productRequestDTO
    ) {
        logger.info("POST /v1/product - Creating product with SKU: {}", productRequestDTO.getSku());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String ownerEmail = authentication.getName();

        logger.debug("Authenticated user: {}", ownerEmail);

        ProductResponseDTO createdProduct = productService.createProduct(productRequestDTO, ownerEmail);

        logger.info("Product created successfully: {}", createdProduct.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable("id") UUID id) {
        logger.info("GET /v1/product/{} - Fetching product", id);

        ProductResponseDTO product = productService.getProductById(id);

        logger.info("Product retrieved successfully");

        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        logger.info("GET /v1/product - Fetching all products");

        List<ProductResponseDTO> products = productService.getAllProducts();

        logger.info("Retrieved {} products", products.size());

        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ProductUpdateDTO productUpdateDTO
    ) {
        logger.info("PUT /v1/product/{} - Updating product", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();

        logger.debug("Authenticated user: {}", authenticatedEmail);

        ProductResponseDTO updatedProduct = productService.updateProduct(id, productUpdateDTO, authenticatedEmail);

        logger.info("Product updated successfully: {}", id);

        return ResponseEntity.ok(updatedProduct);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> patchProduct(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ProductUpdateDTO productUpdateDTO
    ) {
        logger.info("PATCH /v1/product/{} - Updating product", id);

        return updateProduct(id, productUpdateDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") UUID id) {
        logger.info("DELETE /v1/product/{} - Deleting product", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();

        logger.debug("Authenticated user: {}", authenticatedEmail);

        productService.deleteProduct(id, authenticatedEmail);

        logger.info("Product deleted successfully: {}", id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-products")
    public ResponseEntity<List<ProductResponseDTO>> getMyProducts() {
        logger.info("GET /v1/product/my-products - Fetching user's products");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String ownerEmail = authentication.getName();

        logger.debug("Fetching products for user: {}", ownerEmail);

        List<ProductResponseDTO> products = productService.getProductsByOwner(ownerEmail);

        logger.info("User has {} products", products.size());

        return ResponseEntity.ok(products);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.debug("Health check requested");
        return ResponseEntity.ok("Product API is running");
    }
}

/*
*
* Explanation of ProductController:REST API Endpoints created:MethodEndpointAuthPurposePOST/v1/productYESCreate productGET/v1/product/{id}YESGet product by IDGET/v1/productYESGet all productsPUT/v1/product/{id}YES (owner)Update productPATCH/v1/product/{id}YES (owner)Update productDELETE/v1/product/{id}YES (owner)Delete productGET/v1/product/my-productsYESGet my productsMethods explained:1. createProduct() - POST /v1/product
- Any authenticated user can create
- Gets owner email from authentication
- Returns 201 Created2. getProductById() - GET /v1/product/{id}
- Anyone can view
- Takes UUID from URL path
- Returns 200 OK3. getAllProducts() - GET /v1/product
- Anyone can view all products
- Returns array of products
- Returns 200 OK4. updateProduct() - PUT /v1/product/{id}
- Only owner can update
- Service checks ownership
- Returns 200 OK or 403 Forbidden5. patchProduct() - PATCH /v1/product/{id}
- Same as PUT (delegates to it)
- Assignment allows both PUT and PATCH6. deleteProduct() - DELETE /v1/product/{id}
- Only owner can delete
- Service checks ownership
- Returns 204 No Content7. getMyProducts() - GET /v1/product/my-products
- Get products owned by current user
- Optional feature (not required)
- Returns user's products onlyImportant notes:@PathVariable:
java@GetMapping("/{id}")
public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable("id") UUID id)

Extracts {id} from URL
URL: /v1/product/123e4567-...
id = "123e4567-..."
Spring converts String to UUID automatically
Getting authenticated user:
javaAuthentication authentication = SecurityContextHolder.getContext().getAuthentication();
String email = authentication.getName();

Gets email of logged-in user
Used to set owner or check ownership*/