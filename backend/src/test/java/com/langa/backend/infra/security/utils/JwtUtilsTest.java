package com.langa.backend.infra.security.utils;

import com.langa.backend.domain.users.User;
import com.langa.backend.infra.security.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {

        jwtConfig = new JwtConfig("awesome-key-to-match-length-secure-enough-for-hmac-sha-algorithm", "test-kid", 86400000);

        jwtUtils = new JwtUtils(jwtConfig);
    }

    @Test
    void generateToken_shouldContainSubject() {
        User user = new User();
        user.setEmail("user@example.com");

        String token = jwtUtils.generateToken(user);
        assertNotNull(token);

        String usernameFromToken = jwtUtils.getUsernameFromToken(token);
        assertEquals("user@example.com", usernameFromToken);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        User user = new User();
        user.setEmail("user@example.com");

        String token = jwtUtils.generateToken(user);
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String fakeToken = "invalid.token.here";
        assertFalse(jwtUtils.validateToken(fakeToken));
    }
}