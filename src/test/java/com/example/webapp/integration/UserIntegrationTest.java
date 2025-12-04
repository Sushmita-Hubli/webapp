package com.example.webapp.integration;

import com.example.webapp.dto.UserRequestDTO;
import com.example.webapp.dto.UserUpdateDTO;
import com.example.webapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static String testEmail;
    private static String testPassword;
    private static String authHeader;

    @BeforeEach
    public void setup() {
        // Generate unique email for each test
        testEmail = "test" + System.currentTimeMillis() + "@example.com";
        testPassword = "SecurePass123!";
        authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((testEmail + ":" + testPassword).getBytes());
    }

    @AfterEach
    public void cleanup() {
        // Clean up test data
        userRepository.deleteAll();
    }

    // ========================================
    // POSITIVE TEST CASES
    // ========================================

    @Test
    @Order(1)
    @DisplayName("POST /v1/user - Create user with valid data - Should return 201")
    public void testCreateUser_Success() throws Exception {
        UserRequestDTO userRequest = new UserRequestDTO(
                testEmail,
                testPassword,
                "John",
                "Doe"
        );

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.accountCreated").exists())
                .andExpect(jsonPath("$.accountUpdated").exists())
                .andExpect(jsonPath("$.password").doesNotExist()); // Password must NOT be returned
    }

    @Test
    @Order(2)
    @DisplayName("GET /v1/user/self - Get authenticated user - Should return 200")
    public void testGetUser_Success() throws Exception {
        // First create a user
        UserRequestDTO userRequest = new UserRequestDTO(
                testEmail,
                testPassword,
                "Jane",
                "Smith"
        );

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        // Then get the user with authentication
        mockMvc.perform(get("/v1/user/self")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @Order(3)
    @DisplayName("PUT /v1/user/self - Update user with valid data - Should return 200")
    public void testUpdateUser_Success() throws Exception {
        // Create user
        UserRequestDTO userRequest = new UserRequestDTO(
                testEmail,
                testPassword,
                "Original",
                "Name"
        );

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        // Update user
        UserUpdateDTO updateRequest = new UserUpdateDTO(
                "Updated",
                "NameChanged",
                "NewPassword123!"
        );

        mockMvc.perform(put("/v1/user/self")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("NameChanged"));

        // Verify can login with new password
        String newAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((testEmail + ":NewPassword123!").getBytes());

        mockMvc.perform(get("/v1/user/self")
                        .header("Authorization", newAuthHeader))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("PATCH /v1/user/self - Partial update user - Should return 200")
    public void testPatchUser_Success() throws Exception {
        // Create user
        UserRequestDTO userRequest = new UserRequestDTO(
                testEmail,
                testPassword,
                "First",
                "Last"
        );

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        // Patch user
        UserUpdateDTO patchRequest = new UserUpdateDTO(
                "PatchedFirst",
                "PatchedLast",
                testPassword
        );

        mockMvc.perform(patch("/v1/user/self")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("PatchedFirst"))
                .andExpect(jsonPath("$.lastName").value("PatchedLast"));
    }

    // ========================================
    // NEGATIVE TEST CASES
    // ========================================

    @Test
    @DisplayName("POST /v1/user - Create user with duplicate email - Should return 400")
    public void testCreateUser_DuplicateEmail() throws Exception {
        UserRequestDTO userRequest = new UserRequestDTO(
                testEmail,
                testPassword,
                "John",
                "Doe"
        );

        // Create first user
        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        // Try to create duplicate
        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    @DisplayName("POST /v1/user - Create user with invalid email format - Should return 400")
    public void testCreateUser_InvalidEmail() throws Exception {
        UserRequestDTO userRequest = new UserRequestDTO(
                "notanemail",
                testPassword,
                "John",
                "Doe"
        );

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/user - Create user with missing required fields - Should return 400")
    public void testCreateUser_MissingFields() throws Exception {
        String incompleteJson = "{\"email\":\"" + testEmail + "\"}";

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/user - Create user with empty password - Should return 400")
    public void testCreateUser_EmptyPassword() throws Exception {
        UserRequestDTO userRequest = new UserRequestDTO(
                testEmail,
                "",
                "John",
                "Doe"
        );

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /v1/user/self - Unauthorized without credentials - Should return 401")
    public void testGetUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/user/self"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /v1/user/self - Invalid credentials - Should return 401")
    public void testGetUser_InvalidCredentials() throws Exception {
        // Create user
        UserRequestDTO userRequest = new UserRequestDTO(
                testEmail,
                testPassword,
                "John",
                "Doe"
        );

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        // Try with wrong password
        String wrongAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((testEmail + ":WrongPassword").getBytes());

        mockMvc.perform(get("/v1/user/self")
                        .header("Authorization", wrongAuthHeader))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /v1/user/self - Update without authentication - Should return 401")
    public void testUpdateUser_Unauthorized() throws Exception {
        UserUpdateDTO updateRequest = new UserUpdateDTO(
                "Updated",
                "Name",
                "NewPassword123!"
        );

        mockMvc.perform(put("/v1/user/self")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ========================================
    // EDGE CASE TESTS
    // ========================================

    @Test
    @DisplayName("POST /v1/user - Create user with special characters in name - Should return 201")
    public void testCreateUser_SpecialCharacters() throws Exception {
        UserRequestDTO userRequest = new UserRequestDTO(
                testEmail,
                testPassword,
                "José",
                "O'Brien-Smith"
        );

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("José"))
                .andExpect(jsonPath("$.lastName").value("O'Brien-Smith"));
    }

    @Test
    @DisplayName("POST /v1/user - Create user with valid email variations - Should return 201")
    public void testCreateUser_EmailVariations() throws Exception {
        String specialEmail = "test+tag" + System.currentTimeMillis() + "@example.co.uk";
        UserRequestDTO userRequest = new UserRequestDTO(
                specialEmail,
                testPassword,
                "Test",
                "User"
        );

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(specialEmail));
    }

    @Test
    @DisplayName("POST /v1/user - Create user with minimum valid data - Should return 201")
    public void testCreateUser_MinimumData() throws Exception {
        UserRequestDTO userRequest = new UserRequestDTO(
                testEmail,
                "Pass1!",
                "A",
                "B"
        );

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("A"))
                .andExpect(jsonPath("$.lastName").value("B"));
    }

    @Test
    @DisplayName("Verify account_created and account_updated are read-only")
    public void testAccountTimestamps_ReadOnly() throws Exception {
        String jsonWithTimestamps = String.format("""
            {
                "email": "%s",
                "password": "%s",
                "firstName": "John",
                "lastName": "Doe",
                "accountCreated": "2020-01-01T00:00:00",
                "accountUpdated": "2020-01-01T00:00:00"
            }
            """, testEmail, testPassword);

        mockMvc.perform(post("/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithTimestamps))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountCreated").value(not("2020-01-01T00:00:00")))
                .andExpect(jsonPath("$.accountUpdated").value(not("2020-01-01T00:00:00")));
    }

    @Test
    @DisplayName("GET /v1/user/health - Health check - Should return 200")
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/v1/user/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("running")));
    }
}