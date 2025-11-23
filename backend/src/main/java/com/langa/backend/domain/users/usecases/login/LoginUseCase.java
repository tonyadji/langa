package com.langa.backend.domain.users.usecases.login;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.eda.EventPublishingUseCase;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.PasswordService;
import com.langa.backend.domain.users.services.RefreshTokenService;
import com.langa.backend.domain.users.services.TokenProvider;
import com.langa.backend.domain.users.valueobjects.LoginCommand;
import com.langa.backend.domain.users.valueobjects.AuthTokens;

@UseCase
public class LoginUseCase extends EventPublishingUseCase<User> {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginUseCase(UserRepository userRepository,
                        PasswordService passwordService,
                        TokenProvider tokenProvider,
                        RefreshTokenService refreshTokenService,
                        OutboxEventService outboxEventService) {
        super(outboxEventService);
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthTokens execute(LoginCommand command) {
        command.validate();
        final User user = userRepository.findByEmail(command.username())
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));

        if (!passwordService.matches(command.password(), user.getPassword())) {
            throw new UserException("Invalid password", null, Errors.INVALID_CREDENTIALS);
        }

        String accessToken = tokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.issue(user.getUserId().email()).getToken();
        handleDomainEvents(user);
        return new AuthTokens(accessToken, refreshToken);
    }
}
