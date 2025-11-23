package com.langa.backend.domain.users.usecases.firstconnection;

import com.langa.backend.domain.users.valueobjects.UpdatePassword;

public record CompleteRegistrationProcessCommand(
        String firstConnectionToken, UpdatePassword updatePassword
) {
    public void validate() {
        if (firstConnectionToken == null || firstConnectionToken.isBlank()) {
            throw new IllegalArgumentException("First connection token cannot be null or blank");
        }
        if (updatePassword == null) {
            throw new IllegalArgumentException("Update password cannot be null");
        }
    }
}
