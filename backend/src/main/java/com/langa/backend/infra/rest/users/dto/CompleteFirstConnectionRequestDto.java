package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.valueobjects.UpdatePassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompleteFirstConnectionRequestDto(
        @NotBlank @NotNull String firstConnectionToken,
        @NotBlank @NotNull String password,
        @NotBlank @NotNull String confirmationPassword
) {
    public UpdatePassword toUpdatePassword() {
        return new UpdatePassword(password, confirmationPassword);
    }
}
