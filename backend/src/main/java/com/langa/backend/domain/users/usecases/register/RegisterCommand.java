package com.langa.backend.domain.users.usecases.register;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.exceptions.UserException;

import java.util.Objects;

public record RegisterCommand(
        String username, String password, String confirmationPassword) {

    public void validate() {

        if(!Objects.equals(password, confirmationPassword)) {
            throw new UserException("Password do not match", null, Errors.PASSWORDS_MISMATCH);
        }
    }
}
