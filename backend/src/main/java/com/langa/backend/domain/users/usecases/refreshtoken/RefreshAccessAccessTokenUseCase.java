package com.langa.backend.domain.users.usecases.refreshtoken;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.RefreshTokenService;
import com.langa.backend.domain.users.services.TokenProvider;
import com.langa.backend.domain.users.valueobjects.AuthTokens;

@UseCase
public class RefreshAccessAccessTokenUseCase implements IRefreshAccessTokenUseCase, CommandHandler<RefreshAccessTokenCommand, AuthTokens> {

    private final RefreshTokenService refreshTokenService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public RefreshAccessAccessTokenUseCase(RefreshTokenService refreshTokenService,
                                           TokenProvider tokenProvider,
                                           UserRepository userRepository) {
        this.refreshTokenService = refreshTokenService;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public AuthTokens execute(RefreshAccessTokenCommand command) {
        String userEmail = refreshTokenService.validateAndGetUserEmail(command.refreshToken());

        refreshTokenService.rotate(command.refreshToken());

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));
        String newAccessToken = tokenProvider.generateToken(user);
        String newRefreshToken = refreshTokenService.issue(userEmail).getToken();

        return new AuthTokens(newAccessToken, newRefreshToken);
    }

    @Override
    public AuthTokens handle(RefreshAccessTokenCommand command) {
        return execute(command);
    }
}
