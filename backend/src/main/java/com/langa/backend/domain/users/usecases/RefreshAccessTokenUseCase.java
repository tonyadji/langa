package com.langa.backend.domain.users.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.RefreshTokenService;
import com.langa.backend.domain.users.services.TokenProvider;
import com.langa.backend.domain.users.valueobjects.AuthTokens;

@UseCase
public class RefreshAccessTokenUseCase {

    private final RefreshTokenService refreshTokenService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public RefreshAccessTokenUseCase(RefreshTokenService refreshTokenService,
                                     TokenProvider tokenProvider,
                                     UserRepository userRepository) {
        this.refreshTokenService = refreshTokenService;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public AuthTokens refresh(String refreshToken) {
        String userEmail = refreshTokenService.validateAndGetUserEmail(refreshToken);

        refreshTokenService.rotate(refreshToken);

        var user = userRepository.findByEmail(userEmail).orElseThrow();
        String newAccessToken = tokenProvider.generateToken(user);
        String newRefreshToken = refreshTokenService.issue(userEmail).getToken();

        return new AuthTokens(newAccessToken, newRefreshToken);
    }
}
