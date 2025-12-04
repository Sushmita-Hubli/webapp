package com.example.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // ← Add this line!
class WebappApplicationTests {

    @Test
    void contextLoads() {
        // This test just checks if the Spring context loads successfully
    }

}