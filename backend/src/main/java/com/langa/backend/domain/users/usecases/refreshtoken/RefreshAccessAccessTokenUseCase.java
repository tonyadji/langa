package com.langa.backend.domain.users.usecases.refreshtoken;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.domain.users.services.RefreshTokenService;
import com.langa.backend.domain.users.services.TokenProvider;
import com.langa.backend.domain.users.valueobjects.AuthTokens;

@UseCase
public class RefreshAccessAccessTokenUseCase implements IRefreshAccessTokenUseCase, CommandHandler<RefreshAccessTokenCommand, AuthTokens> {

    private final RefreshTokenService refreshTokenService;
    private final TokenProvider tokenProvider;

    public RefreshAccessAccessTokenUseCase(RefreshTokenService refreshTokenService,
                                           TokenProvider tokenProvider) {
        this.refreshTokenService = refreshTokenService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthTokens execute(RefreshAccessTokenCommand command) {
        String userEmail = refreshTokenService.validateAndGetUserEmail(command.refreshToken());

        refreshTokenService.rotate(command.refreshToken());

        String newAccessToken = tokenProvider.generateToken(userEmail);
        String newRefreshToken = refreshTokenService.issue(userEmail).getToken();

        return new AuthTokens(newAccessToken, newRefreshToken);
    }

    @Override
    public AuthTokens handle(RefreshAccessTokenCommand command) {
        return execute(command);
    }
}
