package com.langa.backend.domain.users;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import com.langa.backend.domain.users.repositories.TokenRepository;
import com.langa.backend.domain.users.services.TokenProvider;
import com.langa.backend.domain.users.services.TokenService;
import com.langa.backend.domain.users.valueobjects.Token;
import com.langa.backend.domain.users.valueobjects.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository repository;

    private TokenService service;

    @Mock
    private TokenProvider tokenProvider;

    private Token validToken;

    @BeforeEach
    void setUp() {
        validToken = new Token("refreshToken", "user@example.com",
                Instant.now().plusSeconds(3600), TokenType.REFRESH);

        service = new TokenService(repository, 10, tokenProvider);
    }

    @Test
    void issue_shouldSaveAndReturnRefreshToken() {
        when(repository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenProvider.generateToken(anyString(),any(TokenType.class))).thenReturn("refreshToken");
        Token rt = service.issue(TokenType.REFRESH,"user@example.com");

        assertNotNull(rt);
        assertEquals("user@example.com", rt.getBearer());
        assertFalse(rt.isExpired());
        assertFalse(rt.isRevoked());
    }

    @Test
    void validateAndGetUserEmail_shouldReturnEmail_whenTokenValid() {
        when(repository.findByToken("refreshToken")).thenReturn(Optional.of(validToken));

        String email = service.validateAndGetUserEmail("refreshToken");

        assertEquals("user@example.com", email);
    }

    @Test
    void validateAndGetUserEmail_shouldThrowException_whenTokenNotFound() {
        when(repository.findByToken("unknown")).thenReturn(Optional.empty());

        GenericException ex = assertThrows(GenericException.class,
                () -> service.validateAndGetUserEmail("unknown"));

        assertEquals(Errors.INVALID_CREDENTIALS, ex.getError());
    }

    @Test
    void validateAndGetUserEmail_shouldThrowException_whenTokenExpired() {
        Token expired = new Token("expiredToken", "user@example.com",
                Instant.now().minusSeconds(10), TokenType.REFRESH);

        when(repository.findByToken("expiredToken")).thenReturn(Optional.of(expired));

        GenericException ex = assertThrows(GenericException.class,
                () -> service.validateAndGetUserEmail("expiredToken"));

        assertEquals(Errors.INVALID_CREDENTIALS, ex.getError());
    }

    @Test
    void validateAndGetUserEmail_shouldThrowException_whenTokenRevoked() {
        Token revoked = new Token("revokedToken", "user@example.com",
                Instant.now().plusSeconds(3600), TokenType.REFRESH);
        revoked.revoke();

        when(repository.findByToken("revokedToken")).thenReturn(Optional.of(revoked));

        GenericException ex = assertThrows(GenericException.class,
                () -> service.validateAndGetUserEmail("revokedToken"));

        assertEquals(Errors.INVALID_CREDENTIALS, ex.getError());
    }

    @Test
    void rotate_shouldCallRepositoryRevokeByToken() {
        service.rotate("oldToken123");

        verify(repository, times(1)).revokeByToken("oldToken123");
    }
}
