package com.langa.backend.infra.adapters.persistence.users;

import com.langa.backend.domain.users.valueobjects.Token;
import com.langa.backend.domain.users.valueobjects.TokenType;
import com.langa.backend.infra.adapters.persistence.users.mongo.MongoTokenDao;
import com.langa.backend.infra.adapters.persistence.users.mongo.TokenDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenRepositoryImplTest {

    @Mock
    private MongoTokenDao mongoTokenDao;

    @InjectMocks
    private TokenRepositoryImpl repository;

    @Test
    void revokeByToken_shouldMarkRevoked_whenFound() {
        Token token = new Token("token-1", "user@example.com", Instant.now().plusSeconds(60), TokenType.ACCESS);
        when(mongoTokenDao.findByToken("token-1")).thenReturn(Optional.of(TokenDocument.of(token)));
        when(mongoTokenDao.save(any(TokenDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        repository.revokeByToken("token-1");

        ArgumentCaptor<TokenDocument> captor = ArgumentCaptor.forClass(TokenDocument.class);
        verify(mongoTokenDao).save(captor.capture());
        assertTrue(captor.getValue().isRevoked());
    }

    @Test
    void revokeByToken_shouldDoNothing_whenNotFound() {
        when(mongoTokenDao.findByToken("unknown")).thenReturn(Optional.empty());

        repository.revokeByToken("unknown");

        verify(mongoTokenDao, never()).save(any());
    }

    @Test
    void revokeAllByUserEmail_shouldRevokeEveryToken() {
        Token token1 = new Token("t1", "user@example.com", Instant.now().plusSeconds(60), TokenType.ACCESS);
        Token token2 = new Token("t2", "user@example.com", Instant.now().plusSeconds(60), TokenType.REFRESH);
        when(mongoTokenDao.findByUserEmail("user@example.com"))
                .thenReturn(List.of(TokenDocument.of(token1), TokenDocument.of(token2)));

        repository.revokeAllByUserEmail("user@example.com");

        verify(mongoTokenDao, times(2)).save(any());
    }

    @Test
    void save_shouldPersistAndReturnToken() {
        Token token = new Token("token-1", "user@example.com", Instant.now().plusSeconds(60), TokenType.ACCESS);
        when(mongoTokenDao.save(any(TokenDocument.class))).thenReturn(TokenDocument.of(token));

        Token saved = repository.save(token);

        assertEquals("token-1", saved.getValue());
    }

    @Test
    void findByToken_shouldReturnEmpty_whenNotFound() {
        when(mongoTokenDao.findByToken("unknown")).thenReturn(Optional.empty());
        assertTrue(repository.findByToken("unknown").isEmpty());
    }

    @Test
    void isRevoked_shouldReturnFalse_whenTokenNotFound() {
        when(mongoTokenDao.findByToken("unknown")).thenReturn(Optional.empty());
        assertFalse(repository.isRevoked("unknown"));
    }

    @Test
    void isRevoked_shouldReturnTrue_whenTokenIsRevoked() {
        Token token = new Token("token-1", "user@example.com", Instant.now().plusSeconds(60), true, TokenType.ACCESS);
        when(mongoTokenDao.findByToken("token-1")).thenReturn(Optional.of(TokenDocument.of(token)));

        assertTrue(repository.isRevoked("token-1"));
    }
}
