package com.langa.backend.domain.users.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.RefreshTokenService;
import com.langa.backend.domain.users.TokenProvider;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.valueobjects.AuthRequest;
import com.langa.backend.domain.users.valueobjects.AuthTokens;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginUseCase(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        TokenProvider tokenProvider,
                        RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthTokens login(@Valid AuthRequest loginRequest) {
        final User user = userRepository.findByEmail(loginRequest.username())
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new UserException("Invalid password", null, Errors.INVALID_CREDENTIALS);
        }

        String accessToken = tokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.issue(user.getEmail()).getToken();

        return new AuthTokens(accessToken, refreshToken);
    }
}
