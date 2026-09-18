package com.langa.backend.domain.users.usecases.logout;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.domain.users.services.TokenService;
import com.langa.backend.domain.users.valueobjects.AuthTokens;

@UseCase
public class LogoutUseCase implements ILogoutUseCase, CommandHandler<LogoutCommand, AuthTokens> {

    private final TokenService tokenService;

    public LogoutUseCase(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public AuthTokens handle(LogoutCommand command) {
        return execute(command);
    }

    @Override
    public AuthTokens execute(LogoutCommand command) {
        tokenService.revokeAllByUserEmail(command.username());
        return new AuthTokens(null, null);
    }
}
