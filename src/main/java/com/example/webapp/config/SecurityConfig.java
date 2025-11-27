package com.example.webapp.config;

import com.example.webapp.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring Security Filter Chain");

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("POST", "/v1/user").permitAll()
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .httpBasic(basic -> {
                    logger.debug("HTTP Basic Authentication enabled");
                });

        logger.info("Security Filter Chain configured successfully");

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        logger.info("Configuring DAO Authentication Provider");

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        logger.info("Authentication Provider configured successfully");

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        logger.debug("Creating Authentication Manager");
        return authConfig.getAuthenticationManager();
    }
}


/*
* Explanation of SecurityConfig:
What is this file?

Configures Spring Security
Defines which endpoints require authentication
Enables Basic Authentication
Disables sessions (stateless)

securityFilterChain() method explained:
1. CSRF disabled:
java.csrf(csrf -> csrf.disable())

CSRF = Cross-Site Request Forgery protection
We disable it for REST APIs
Assignment requirement: stateless authentication

2. Authorization rules:
java.authorizeHttpRequests(auth -> auth
    .requestMatchers("POST", "/v1/user").permitAll()  // Anyone can create account
    .anyRequest().authenticated()                      // Everything else needs login
)
Public endpoints:

POST /v1/user - Creating user account (no login needed)

Protected endpoints:

GET /v1/user/self - Get user info (login required)
PUT /v1/user/self - Update user (login required)
All product endpoints (login required)

3. Session management:
java.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)

STATELESS = no sessions created
No cookies stored
Must send credentials with EVERY request
Assignment requirement: "Token-Based authentication not Session Authentication"

4. HTTP Basic Authentication:
java.httpBasic(basic -> {
    logger.debug("HTTP Basic Authentication enabled");
})
```
- Enables Basic Auth
- Client sends: `Authorization: Basic base64(email:password)`
- Server verifies credentials for each request

**authenticationProvider() method:**
- Tells Spring Security how to authenticate
- Uses CustomUserDetailsService to load users
- Uses BCryptPasswordEncoder to verify passwords

**How authentication works:**
```
1. Client sends: Authorization: Basic am9obkBleGFtcGxlLmNvbTpwYXNzd29yZA==
2. Spring decodes: john@example.com:password
3. Calls CustomUserDetailsService.loadUserByUsername("john@example.com")
4. Gets hashed password from database
5. Compares passwords using BCrypt
6. If match → authenticated ✅
7. If no match → 401 Unauthorized ❌*/