package com.langa.backend.domain.users.usecases.register;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.commands.Command;
import com.langa.backend.domain.users.exceptions.UserException;

import java.util.Objects;
import java.util.regex.Pattern;

public record RegisterUserCommand(
        String username, String password, String confirmationPassword
) implements Command<String> {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,128}$");

    public RegisterUserCommand {

        if (username == null || username.isEmpty()) {
            throw new UserException("Username is required", null, Errors.VALIDATION_ERROR);
        }

        if (password == null || password.isEmpty()) {
            throw new UserException("Password is required", null, Errors.VALIDATION_ERROR);
        }

        if (confirmationPassword == null || confirmationPassword.isEmpty()) {
            throw new UserException("Confirmation is required", null, Errors.VALIDATION_ERROR);
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new UserException(
                    "Password must be 8-128 characters long and contain at least one letter and one digit",
                    null, Errors.VALIDATION_ERROR);
        }

        if(!Objects.equals(password, confirmationPassword)) {
            throw new UserException("Password do not match", null, Errors.PASSWORDS_MISMATCH);
        }
    }
}
