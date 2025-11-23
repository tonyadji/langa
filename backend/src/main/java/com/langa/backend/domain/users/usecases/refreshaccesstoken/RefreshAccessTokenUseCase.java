package com.langa.backend.domain.users.usecases.refreshaccesstoken;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.eda.EventPublishingUseCase;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.RefreshTokenService;
import com.langa.backend.domain.users.services.TokenProvider;
import com.langa.backend.domain.users.valueobjects.AuthTokens;

@UseCase
public class RefreshAccessTokenUseCase extends EventPublishingUseCase<User> {

    private final RefreshTokenService refreshTokenService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public RefreshAccessTokenUseCase(RefreshTokenService refreshTokenService,
                                     OutboxEventService outboxEventService,
                                     TokenProvider tokenProvider,
                                     UserRepository userRepository) {
        super(outboxEventService);
        this.refreshTokenService = refreshTokenService;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public AuthTokens execute(RefreshAccessTokenCommand command) {
        command.validate();

        String userEmail = refreshTokenService.validateAndGetUserEmail(command.refreshToken());

        refreshTokenService.rotate(command.refreshToken());

        var user = userRepository.findByEmail(userEmail).orElseThrow();

        String newAccessToken = tokenProvider.generateToken(user);
        String newRefreshToken = refreshTokenService.issue(userEmail).getToken();

        handleDomainEvents(user);

        return new AuthTokens(newAccessToken, newRefreshToken);
    }
}
