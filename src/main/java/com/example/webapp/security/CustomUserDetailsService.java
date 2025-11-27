package com.example.webapp.security;

import com.example.webapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username (email): {}", username);

        com.example.webapp.model.User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });

        logger.debug("User found: {} {}", user.getFirstName(), user.getLastName());

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        logger.debug("Authorities assigned: {}", authorities);

        UserDetails userDetails = new User(
                user.getEmail(),
                user.getPassword(),
                true,
                true,
                true,
                true,
                authorities
        );

        logger.info("UserDetails created successfully for user: {}", username);

        return userDetails;
    }
}

/*Explanation of CustomUserDetailsService:What is this file?

Tells Spring Security how to load users from database
Required for authentication
How authentication works:1. User sends request with email:password
2. Spring Security calls loadUserByUsername(email)
3. This method finds user in database
4. Returns UserDetails with hashed password
5. Spring Security compares passwords
6. If match → authenticated ✅
7. If no match → 401 Unauthorized ❌loadUserByUsername() method:
Step 1: Find user by email in database
Step 2: If not found → throw UsernameNotFoundException
Step 3: Create authorities (roles) - we use "ROLE_USER"
Step 4: Create UserDetails object with user info
Step 5: Return UserDetails to Spring SecurityImportant notes:
username parameter = email (we use email as username)
User constructor parameters:

email (username)
hashed password from database
enabled (true)
accountNonExpired (true)
credentialsNonExpired (true)
accountNonLocked (true)
authorities (ROLE_USER)


WARNING: There are TWO User classes:

com.example.webapp.model.User - OUR user entity
org.springframework.security.core.userdetails.User - Spring Security's User
We use Spring Security's User in the constructor.*/