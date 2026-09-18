package com.langa.backend.domain.users.usecases.refreshtoken;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.TokenService;
import com.langa.backend.domain.users.valueobjects.AuthTokens;
import com.langa.backend.domain.users.valueobjects.Token;
import com.langa.backend.domain.users.valueobjects.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private TokenService tokenService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenUseCase useCase;

    @Test
    void execute_shouldIssueNewTokens_whenRefreshTokenValid() {
        User user = User.createActive("user@example.com", "encoded");
        when(tokenService.validateAndGetUserEmail("refresh-token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenService.issue(eq(TokenType.ACCESS), any(User.class)))
                .thenReturn(new Token("new-access", user.getEmail(), Instant.now().plusSeconds(60), TokenType.ACCESS));
        when(tokenService.issue(eq(TokenType.REFRESH), any(User.class)))
                .thenReturn(new Token("new-refresh", user.getEmail(), Instant.now().plusSeconds(60), TokenType.REFRESH));

        AuthTokens tokens = useCase.execute(new RefreshAccessTokenCommand("refresh-token"));

        assertEquals("new-access", tokens.accessToken());
        assertEquals("new-refresh", tokens.refreshToken());
    }

    @Test
    void execute_shouldThrow_whenUserNotFound() {
        when(tokenService.validateAndGetUserEmail("refresh-token")).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class,
                () -> useCase.execute(new RefreshAccessTokenCommand("refresh-token")));

        assertEquals(Errors.USER_NOT_FOUND, ex.getError());
    }

    @Test
    void handle_shouldDelegateToExecute() {
        User user = User.createActive("user@example.com", "encoded");
        when(tokenService.validateAndGetUserEmail("refresh-token")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenService.issue(eq(TokenType.ACCESS), any(User.class)))
                .thenReturn(new Token("new-access", user.getEmail(), Instant.now().plusSeconds(60), TokenType.ACCESS));
        when(tokenService.issue(eq(TokenType.REFRESH), any(User.class)))
                .thenReturn(new Token("new-refresh", user.getEmail(), Instant.now().plusSeconds(60), TokenType.REFRESH));

        AuthTokens tokens = useCase.handle(new RefreshAccessTokenCommand("refresh-token"));

        assertNotNull(tokens);
    }
}
