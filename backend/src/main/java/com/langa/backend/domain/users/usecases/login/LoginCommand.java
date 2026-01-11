package com.langa.backend.domain.users.usecases.login;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.AuthTokens;

public record LoginCommand(
        String username, String password
) implements Command<AuthTokens> {

    public LoginCommand {
        if (username == null || username.isEmpty()) {
            throw new UserException("Username is required", null, Errors.VALIDATION_ERROR);
        }

        if (password == null || password.isEmpty()) {
            throw new UserException("Password is required", null, Errors.VALIDATION_ERROR);
        }
    }
}
