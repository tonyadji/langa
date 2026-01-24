package com.langa.backend.domain.users.usecases.refreshtoken;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.TokenService;
import com.langa.backend.domain.users.valueobjects.AuthTokens;
import com.langa.backend.domain.users.valueobjects.TokenType;

@UseCase
public class RefreshTokenUseCase implements IRefreshAccessTokenUseCase, CommandHandler<RefreshAccessTokenCommand, AuthTokens> {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public RefreshTokenUseCase(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
      this.userRepository = userRepository;
    }

    @Override
    public AuthTokens execute(RefreshAccessTokenCommand command) {
        String userEmail = tokenService.validateAndGetUserEmail(command.refreshToken());

        final User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));

        tokenService.revokeAllByUserEmail(userEmail);

        String newAccessToken = tokenService.issue(TokenType.ACCESS, user).getValue();
        String newRefreshToken = tokenService.issue(TokenType.REFRESH, user).getValue();

        return new AuthTokens(newAccessToken, newRefreshToken);
    }

    @Override
    public AuthTokens handle(RefreshAccessTokenCommand command) {
        return execute(command);
    }
}
