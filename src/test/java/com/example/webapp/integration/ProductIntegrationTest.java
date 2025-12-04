package com.example.webapp.integration;

import com.example.webapp.dto.ProductRequestDTO;
import com.example.webapp.dto.ProductUpdateDTO;
import com.example.webapp.dto.UserRequestDTO;
import com.example.webapp.model.Product;
import com.example.webapp.repository.ProductRepository;
import com.example.webapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private static String user1Email;
    private static String user1Password;
    private static String user1AuthHeader;

    private static String user2Email;
    private static String user2Password;
    private static String user2AuthHeader;

    @BeforeAll
    public static void setupUsers() {
        user1Email = "productowner1" + System.currentTimeMillis() + "@example.com";
        user1Password = "SecurePass123!";
        user1AuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((user1Email + ":" + user1Password).getBytes());

        user2Email = "productowner2" + System.currentTimeMillis() + "@example.com";
        user2Password = "SecurePass456!";
        user2AuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((user2Email + ":" + user2Password).getBytes());
    }

    @BeforeEach
    public void setup() throws Exception {
        // Clean database
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        createUser(user1Email, user1Password, "User", "One");
        createUser(user2Email, user2Password, "User", "Two");
    }

    @AfterEach
    public void cleanup() {
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createUser(String email, String password, String firstName, String lastName) throws Exception {
        UserRequestDTO userRequest = new UserRequestDTO(email, password, firstName, lastName);
        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());
    }

    // ========================================
    // POSITIVE TEST CASES
    // ========================================

    @Test
    @Order(1)
    @DisplayName("POST /v1/product - Create product with valid data - Should return 201")
    public void testCreateProduct_Success() throws Exception {
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "Test Laptop",
                "High performance laptop",
                "LAP-" + System.currentTimeMillis(),
                "Dell",
                50
        );

        mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Laptop"))
                .andExpect(jsonPath("$.description").value("High performance laptop"))
                .andExpect(jsonPath("$.sku").value(productRequest.getSku()))
                .andExpect(jsonPath("$.manufacturer").value("Dell"))
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.dateAdded").exists())
                .andExpect(jsonPath("$.dateLastUpdated").exists())
                .andExpect(jsonPath("$.ownerUserId").exists());
    }

    @Test
    @Order(2)
    @DisplayName("GET /v1/product/{id} - Get product by ID - Should return 200")
    public void testGetProductById_Success() throws Exception {
        // Create product
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "Laptop",
                "Gaming laptop",
                "LAP-" + System.currentTimeMillis(),
                "ASUS",
                25
        );

        MvcResult createResult = mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String productId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // Get product by ID
        mockMvc.perform(get("/v1/product/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.manufacturer").value("ASUS"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /v1/product - Get all products - Should return 200")
    public void testGetAllProducts_Success() throws Exception {
        // Create multiple products
        for (int i = 0; i < 3; i++) {
            ProductRequestDTO productRequest = new ProductRequestDTO(
                    "Product " + i,
                    "Description " + i,
                    "SKU-" + System.currentTimeMillis() + "-" + i,
                    "Manufacturer",
                    10 + i
            );

            mockMvc.perform(post("/v1/product")
                            .header("Authorization", user1AuthHeader)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productRequest)))
                    .andExpect(status().isCreated());
        }

        // Get all products
        mockMvc.perform(get("/v1/product"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @Order(4)
    @DisplayName("PUT /v1/product/{id} - Update own product - Should return 200")
    public void testUpdateProduct_Success() throws Exception {
        // Create product
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "Original Product",
                "Original description",
                "SKU-" + System.currentTimeMillis(),
                "Original Manufacturer",
                100
        );

        MvcResult createResult = mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String productId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // Update product
        ProductUpdateDTO updateRequest = new ProductUpdateDTO(
                "Updated Product",
                "Updated description",
                productRequest.getSku(),
                "Updated Manufacturer",
                150
        );

        mockMvc.perform(put("/v1/product/" + productId)
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.manufacturer").value("Updated Manufacturer"))
                .andExpect(jsonPath("$.quantity").value(150));
    }

    @Test
    @Order(5)
    @DisplayName("PATCH /v1/product/{id} - Partial update product - Should return 200")
    public void testPatchProduct_Success() throws Exception {
        // Create product
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "Laptop",
                "Description",
                "SKU-" + System.currentTimeMillis(),
                "Dell",
                100
        );

        MvcResult createResult = mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String productId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // Patch product
        ProductUpdateDTO patchRequest = new ProductUpdateDTO(
                "Laptop",
                "Description",
                productRequest.getSku(),
                "Dell",
                200
        );

        mockMvc.perform(patch("/v1/product/" + productId)
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(200));
    }

    @Test
    @Order(6)
    @DisplayName("DELETE /v1/product/{id} - Delete own product - Should return 204")
    public void testDeleteProduct_Success() throws Exception {
        // Create product
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "Product to Delete",
                "Will be deleted",
                "DEL-" + System.currentTimeMillis(),
                "Manufacturer",
                10
        );

        MvcResult createResult = mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String productId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // Delete product
        mockMvc.perform(delete("/v1/product/" + productId)
                        .header("Authorization", user1AuthHeader))
                .andExpect(status().isNoContent());

        // Verify product is deleted
        mockMvc.perform(get("/v1/product/" + productId))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // NEGATIVE TEST CASES
    // ========================================

    @Test
    @DisplayName("POST /v1/product - Create without authentication - Should return 401")
    public void testCreateProduct_Unauthorized() throws Exception {
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "Unauthorized Product",
                "Should not be created",
                "UNAUTH-001",
                "Manufacturer",
                10
        );

        mockMvc.perform(post("/v1/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /v1/product - Create with missing required fields - Should return 400")
    public void testCreateProduct_MissingFields() throws Exception {
        String incompleteJson = "{\"name\":\"Incomplete Product\"}";

        mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/product - Create with invalid quantity (negative) - Should return 400")
    public void testCreateProduct_NegativeQuantity() throws Exception {
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "Invalid Product",
                "Negative quantity",
                "INV-" + System.currentTimeMillis(),
                "Manufacturer",
                -10
        );

        mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/product - Create with duplicate SKU - Should return 400")
    public void testCreateProduct_DuplicateSKU() throws Exception {
        String duplicateSku = "DUP-" + System.currentTimeMillis();

        ProductRequestDTO productRequest1 = new ProductRequestDTO(
                "Product 1",
                "First product",
                duplicateSku,
                "Manufacturer",
                10
        );

        // Create first product
        mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest1)))
                .andExpect(status().isCreated());

        // Try to create with same SKU
        ProductRequestDTO productRequest2 = new ProductRequestDTO(
                "Product 2",
                "Second product",
                duplicateSku,
                "Manufacturer",
                20
        );

        mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    @DisplayName("GET /v1/product/{id} - Get non-existent product - Should return 404")
    public void testGetProduct_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/v1/product/" + randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /v1/product/{id} - Update non-existent product - Should return 404")
    public void testUpdateProduct_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        ProductUpdateDTO updateRequest = new ProductUpdateDTO(
                "Updated",
                "Description",
                "SKU-001",
                "Manufacturer",
                10
        );

        mockMvc.perform(put("/v1/product/" + randomId)
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /v1/product/{id} - Update product owned by another user - Should return 403")
    public void testUpdateProduct_Forbidden() throws Exception {
        // User 1 creates product
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "User1 Product",
                "Owned by user 1",
                "OWN-" + System.currentTimeMillis(),
                "Manufacturer",
                10
        );

        MvcResult createResult = mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String productId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // User 2 tries to update User 1's product
        ProductUpdateDTO updateRequest = new ProductUpdateDTO(
                "Hacked Product",
                "Trying to hack",
                productRequest.getSku(),
                "Hacker",
                999
        );

        mockMvc.perform(put("/v1/product/" + productId)
                        .header("Authorization", user2AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /v1/product/{id} - Delete product owned by another user - Should return 403")
    public void testDeleteProduct_Forbidden() throws Exception {
        // User 1 creates product
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "User1 Product",
                "Owned by user 1",
                "DEL-" + System.currentTimeMillis(),
                "Manufacturer",
                10
        );

        MvcResult createResult = mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String productId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // User 2 tries to delete User 1's product
        mockMvc.perform(delete("/v1/product/" + productId)
                        .header("Authorization", user2AuthHeader))
                .andExpect(status().isForbidden());
    }

    // ========================================
    // EDGE CASE TESTS
    // ========================================

    @Test
    @DisplayName("POST /v1/product - Create with zero quantity - Should return 201")
    public void testCreateProduct_ZeroQuantity() throws Exception {
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "Zero Stock Product",
                "Out of stock",
                "ZERO-" + System.currentTimeMillis(),
                "Manufacturer",
                0
        );

        mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(0));
    }

    @Test
    @DisplayName("POST /v1/product - Create with special characters - Should return 201")
    public void testCreateProduct_SpecialCharacters() throws Exception {
        ProductRequestDTO productRequest = new ProductRequestDTO(
                "Laptop & Desktop (2024)",
                "High-end product with 100% satisfaction!",
                "SPEC-" + System.currentTimeMillis(),
                "Dell & HP",
                50
        );

        mockMvc.perform(post("/v1/product")
                        .header("Authorization", user1AuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop & Desktop (2024)"));
    }

    @Test
    @DisplayName("GET /v1/product/my-products - Get user's products - Should return 200")
    public void testGetMyProducts_Success() throws Exception {
        // Create 2 products for user1
        for (int i = 0; i < 2; i++) {
            ProductRequestDTO productRequest = new ProductRequestDTO(
                    "My Product " + i,
                    "Description",
                    "MY-" + System.currentTimeMillis() + "-" + i,
                    "Manufacturer",
                    10
            );

            mockMvc.perform(post("/v1/product")
                            .header("Authorization", user1AuthHeader)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productRequest)))
                    .andExpect(status().isCreated());
        }

        // Get user1's products
        mockMvc.perform(get("/v1/product/my-products")
                        .header("Authorization", user1AuthHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /v1/product/health - Health check - Should return 200")
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/v1/product/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("running")));
    }
}