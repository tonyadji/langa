package com.langa.backend.infra.security.utils;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.repositories.TokenRepository;
import com.langa.backend.domain.users.valueobjects.TokenType;
import com.langa.backend.infra.security.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {

        jwtConfig = new JwtConfig("awesome-key-to-match-length-secure-enough-for-hmac-sha-algorithm", "test-kid", 86400000);

        jwtUtils = new JwtUtils(jwtConfig, null);
    }

    @Test
    void generateToken_shouldContainSubject() {
        User user = User.createNew("user@example.com", "encodedPassword");

        String token = jwtUtils.generateToken(user, TokenType.ACCESS);
        assertNotNull(token);

        String usernameFromToken = jwtUtils.getUsernameFromToken(token);
        assertEquals("user@example.com", usernameFromToken);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        User user = User.createNew("user@example.com", "encodedPassword");
        TokenRepository tokenRepository = Mockito.mock(TokenRepository.class);
        when(tokenRepository.isRevoked(anyString())).thenReturn(false);
        jwtUtils = new JwtUtils(jwtConfig, tokenRepository);
        String token = jwtUtils.generateToken(user, TokenType.ACCESS);
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String fakeToken = "invalid.token.here";
        assertFalse(jwtUtils.validateToken(fakeToken));
    }
}