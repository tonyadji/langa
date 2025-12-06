package com.langa.backend.domain.users.usecases.login;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.RefreshTokenService;
import com.langa.backend.domain.users.services.TokenProvider;
import com.langa.backend.domain.users.valueobjects.AuthTokens;
import org.springframework.security.crypto.password.PasswordEncoder;

@UseCase
public class LoginUseCase implements ILoginUseCase, CommandHandler<LoginCommand, AuthTokens> {

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

    @Override
    public AuthTokens execute(LoginCommand command) {
        final User user = userRepository.findByEmail(command.username())
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new UserException("Invalid password", null, Errors.INVALID_CREDENTIALS);
        }

        String accessToken = tokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.issue(user.getEmail()).getToken();

        return new AuthTokens(accessToken, refreshToken);
    }

    @Override
    public AuthTokens handle(LoginCommand command) {
        return execute(command);
    }
}
