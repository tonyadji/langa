package com.langa.backend.domain.users.usecases.login;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.LoginAttemptLimiter;
import com.langa.backend.domain.users.services.TokenService;
import com.langa.backend.domain.users.valueobjects.AuthTokens;
import com.langa.backend.domain.users.valueobjects.TokenType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@UseCase
public class LoginUseCase implements ILoginUseCase, CommandHandler<LoginCommand, AuthTokens> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginAttemptLimiter loginAttemptLimiter;
    private final String dummyPasswordHash;

    public LoginUseCase(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        TokenService tokenService,
                        LoginAttemptLimiter loginAttemptLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.loginAttemptLimiter = loginAttemptLimiter;
        // Used to keep the login timing constant whether the account exists or not,
        // and to avoid running matches() against a null hash.
        this.dummyPasswordHash = passwordEncoder.encode("no-such-account-" + UUID.randomUUID());
    }

    @Override
    public AuthTokens execute(LoginCommand command) {
        if (loginAttemptLimiter.isBlocked(command.username())) {
            throw new UserException("Too many login attempts", null, Errors.TOO_MANY_LOGIN_ATTEMPTS);
        }

        final User user = userRepository.findByEmail(command.username()).orElse(null);
        final String passwordHash = user != null ? user.getPassword() : dummyPasswordHash;

        if (user == null || !passwordEncoder.matches(command.password(), passwordHash)) {
            loginAttemptLimiter.recordFailure(command.username());
            throw new UserException("Invalid credentials", null, Errors.INVALID_CREDENTIALS);
        }

        loginAttemptLimiter.recordSuccess(command.username());

        String accessToken = tokenService.issue(TokenType.ACCESS, user).getValue();
        String refreshToken = tokenService.issue(TokenType.REFRESH, user).getValue();

        return new AuthTokens(accessToken, refreshToken);
    }

    @Override
    public AuthTokens handle(LoginCommand command) {
        return execute(command);
    }
}
