package com.langa.backend.domain.users.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.RefreshToken;
import com.langa.backend.domain.users.RefreshTokenService;
import com.langa.backend.domain.users.TokenProvider;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.valueobjects.AuthRequest;
import com.langa.backend.domain.users.valueobjects.AuthTokens; // <-- correction ici
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private LoginUseCase loginUseCase;

    @Test
    void login_shouldReturnAuthTokens_whenCredentialsAreValid() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn("jwt-access-token");
        when(refreshTokenService.issue(user.getEmail())).thenReturn(new RefreshToken("jwt-refresh-token", "user@example.com", Instant.now()));

        AuthRequest request = new AuthRequest("user@example.com", "rawPassword");
        AuthTokens tokens = loginUseCase.login(request);

        assertEquals("jwt-access-token", tokens.accessToken());
        assertEquals("jwt-refresh-token", tokens.refreshToken());
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        AuthRequest request = new AuthRequest("unknown@example.com", "pwd");

        UserException ex = assertThrows(UserException.class,
                () -> loginUseCase.login(request));

        assertEquals(Errors.USER_NOT_FOUND, ex.getError());
    }

    @Test
    void login_shouldThrowException_whenPasswordInvalid() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        AuthRequest request = new AuthRequest("user@example.com", "wrongPassword");

        UserException ex = assertThrows(UserException.class,
                () -> loginUseCase.login(request));

        assertEquals(Errors.INVALID_CREDENTIALS, ex.getError());
    }
}
