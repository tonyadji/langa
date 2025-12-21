package com.langa.backend.domain.users.valueobjects;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.exceptions.UserException;

import java.util.Objects;

public record UpdatePassword(
        String password,
        String confirmationPassword
) {
    public UpdatePassword {
        if (password == null || password.isEmpty()) {
            throw new UserException("Password is required", null, Errors.VALIDATION_ERROR);
        }

        if (confirmationPassword == null || confirmationPassword.isEmpty()) {
            throw new UserException("Confirmation Password is required", null, Errors.VALIDATION_ERROR);
        }

        if (!Objects.equals(password,confirmationPassword)) {
            throw new UserException("Passwords do not match", null, Errors.PASSWORDS_MISMATCH);
        }
    }
}
