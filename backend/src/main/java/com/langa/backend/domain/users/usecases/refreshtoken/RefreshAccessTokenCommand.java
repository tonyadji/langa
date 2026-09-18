package com.langa.backend.domain.users.usecases.refreshtoken;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.AuthTokens;

public record RefreshAccessTokenCommand(
        String refreshToken
) implements Command<AuthTokens> {
    public RefreshAccessTokenCommand {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new UserException("refreshToken is required", null, Errors.VALIDATION_ERROR);
        }
    }
}
