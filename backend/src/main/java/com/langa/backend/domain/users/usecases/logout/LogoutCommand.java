package com.langa.backend.domain.users.usecases.logout;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.AuthTokens;

public record LogoutCommand(
        String username
) implements Command<AuthTokens> {

    public LogoutCommand {
        if (username == null || username.isEmpty()) {
            throw new UserException("Username is required", null, Errors.VALIDATION_ERROR);
        }
    }
}
