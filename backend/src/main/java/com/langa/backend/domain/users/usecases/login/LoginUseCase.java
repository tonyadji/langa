package com.langa.backend.domain.users.usecases.login;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.TokenService;
import com.langa.backend.domain.users.valueobjects.AuthTokens;
import com.langa.backend.domain.users.valueobjects.TokenType;
import org.springframework.security.crypto.password.PasswordEncoder;

@UseCase
public class LoginUseCase implements ILoginUseCase, CommandHandler<LoginCommand, AuthTokens> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginUseCase(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public AuthTokens execute(LoginCommand command) {
        final User user = userRepository.findByEmail(command.username())
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new UserException("Invalid password", null, Errors.INVALID_CREDENTIALS);
        }

        String accessToken = tokenService.issue(TokenType.ACCESS, user).getValue();
        String refreshToken = tokenService.issue(TokenType.REFRESH, user).getValue();

        return new AuthTokens(accessToken, refreshToken);
    }

    @Override
    public AuthTokens handle(LoginCommand command) {
        return execute(command);
    }
}
