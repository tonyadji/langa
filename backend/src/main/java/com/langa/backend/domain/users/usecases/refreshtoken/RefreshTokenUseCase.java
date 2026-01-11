package com.langa.backend.domain.users.usecases.refreshtoken;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.domain.users.services.TokenService;
import com.langa.backend.domain.users.valueobjects.AuthTokens;
import com.langa.backend.domain.users.valueobjects.TokenType;

@UseCase
public class RefreshTokenUseCase implements IRefreshAccessTokenUseCase, CommandHandler<RefreshAccessTokenCommand, AuthTokens> {

    private final TokenService tokenService;

    public RefreshTokenUseCase(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public AuthTokens execute(RefreshAccessTokenCommand command) {
        String userEmail = tokenService.validateAndGetUserEmail(command.refreshToken());

        tokenService.revokeAllByUserEmail(userEmail);

        String newAccessToken = tokenService.issue(TokenType.ACCESS, userEmail).getValue();
        String newRefreshToken = tokenService.issue(TokenType.REFRESH, userEmail).getValue();

        return new AuthTokens(newAccessToken, newRefreshToken);
    }

    @Override
    public AuthTokens handle(RefreshAccessTokenCommand command) {
        return execute(command);
    }
}
