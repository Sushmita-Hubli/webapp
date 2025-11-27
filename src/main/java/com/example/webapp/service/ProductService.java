package com.example.webapp.service;

import com.example.webapp.dto.ProductRequestDTO;
import com.example.webapp.dto.ProductResponseDTO;
import com.example.webapp.dto.ProductUpdateDTO;
import com.example.webapp.model.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO, String ownerEmail);

    ProductResponseDTO getProductById(UUID id);

    List<ProductResponseDTO> getAllProducts();

    List<ProductResponseDTO> getProductsByOwner(String ownerEmail);

    ProductResponseDTO updateProduct(UUID id, ProductUpdateDTO productUpdateDTO, String authenticatedEmail);

    void deleteProduct(UUID id, String authenticatedEmail);

    boolean isOwner(Product product, String userEmail);
}