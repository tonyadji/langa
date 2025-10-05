package com.langa.backend.domain.users;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import com.langa.backend.domain.users.repositories.RefreshTokenRepository;
import com.langa.backend.domain.users.services.RefreshTokenService;
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
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    private RefreshTokenService service;

    private RefreshToken validToken;

    @BeforeEach
    void setUp() {
        validToken = new RefreshToken("refreshToken", "user@example.com",
                Instant.now().plusSeconds(3600));

        service = new RefreshTokenService(repository, 10);
    }

    @Test
    void issue_shouldSaveAndReturnRefreshToken() {
        when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken rt = service.issue("user@example.com");

        assertNotNull(rt);
        assertEquals("user@example.com", rt.getUserEmail());
        assertFalse(rt.isExpired());
        assertFalse(rt.isRevoked());
        verify(repository, times(1)).save(any(RefreshToken.class));
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
        RefreshToken expired = new RefreshToken("expiredToken", "user@example.com",
                Instant.now().minusSeconds(10));

        when(repository.findByToken("expiredToken")).thenReturn(Optional.of(expired));

        GenericException ex = assertThrows(GenericException.class,
                () -> service.validateAndGetUserEmail("expiredToken"));

        assertEquals(Errors.INVALID_CREDENTIALS, ex.getError());
    }

    @Test
    void validateAndGetUserEmail_shouldThrowException_whenTokenRevoked() {
        RefreshToken revoked = new RefreshToken("revokedToken", "user@example.com",
                Instant.now().plusSeconds(3600));
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
