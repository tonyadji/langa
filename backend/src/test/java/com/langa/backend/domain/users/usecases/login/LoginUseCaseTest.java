package com.langa.backend.domain.users.usecases.login;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.LoginAttemptLimiter;
import com.langa.backend.domain.users.services.TokenService;
import com.langa.backend.domain.users.valueobjects.AuthTokens;
import com.langa.backend.domain.users.valueobjects.Token;
import com.langa.backend.domain.users.valueobjects.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    @Mock
    private LoginAttemptLimiter loginAttemptLimiter;

    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$dummyHashForTimingNormalization");
        loginUseCase = new LoginUseCase(userRepository, passwordEncoder, tokenService, loginAttemptLimiter);
    }

    private User activeUser() {
        return User.createActive("user@example.com", "encoded-password");
    }

    @Test
    void execute_shouldReturnTokens_whenCredentialsAreValid() {
        User user = activeUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(tokenService.issue(eq(TokenType.ACCESS), any(User.class)))
                .thenReturn(new Token("access-token", user.getEmail(), Instant.now().plusSeconds(60), TokenType.ACCESS));
        when(tokenService.issue(eq(TokenType.REFRESH), any(User.class)))
                .thenReturn(new Token("refresh-token", user.getEmail(), Instant.now().plusSeconds(60), TokenType.REFRESH));

        AuthTokens tokens = loginUseCase.execute(new LoginCommand("user@example.com", "password123"));

        assertEquals("access-token", tokens.accessToken());
        assertEquals("refresh-token", tokens.refreshToken());
        verify(loginAttemptLimiter).recordSuccess("user@example.com");
        verify(loginAttemptLimiter, never()).recordFailure(anyString());
    }

    @Test
    void execute_shouldThrowInvalidCredentials_whenUserDoesNotExist() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class,
                () -> loginUseCase.execute(new LoginCommand("unknown@example.com", "whatever")));

        assertEquals(Errors.INVALID_CREDENTIALS, ex.getError());
        verify(loginAttemptLimiter).recordFailure("unknown@example.com");
        verify(tokenService, never()).issue(any(TokenType.class), any(User.class));
    }

    @Test
    void execute_shouldThrowInvalidCredentials_whenPasswordIsWrong() {
        User user = activeUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        UserException ex = assertThrows(UserException.class,
                () -> loginUseCase.execute(new LoginCommand("user@example.com", "wrong-password")));

        assertEquals(Errors.INVALID_CREDENTIALS, ex.getError());
        verify(loginAttemptLimiter).recordFailure("user@example.com");
    }

    @Test
    void execute_shouldNotDistinguishUnknownUserFromWrongPassword() {
        User user = activeUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UserException wrongPassword = assertThrows(UserException.class,
                () -> loginUseCase.execute(new LoginCommand("user@example.com", "wrong-password")));
        UserException unknownUser = assertThrows(UserException.class,
                () -> loginUseCase.execute(new LoginCommand("unknown@example.com", "whatever")));

        assertEquals(wrongPassword.getError(), unknownUser.getError());
    }

    @Test
    void execute_shouldThrowTooManyAttempts_whenAccountIsBlocked() {
        when(loginAttemptLimiter.isBlocked("user@example.com")).thenReturn(true);

        UserException ex = assertThrows(UserException.class,
                () -> loginUseCase.execute(new LoginCommand("user@example.com", "password123")));

        assertEquals(Errors.TOO_MANY_LOGIN_ATTEMPTS, ex.getError());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(tokenService);
    }
}
