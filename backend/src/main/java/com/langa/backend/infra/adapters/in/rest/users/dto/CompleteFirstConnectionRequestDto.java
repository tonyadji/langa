package com.langa.backend.infra.adapters.in.rest.users.dto;

import com.langa.backend.domain.users.usecases.firstconnection.CompleteRegistrationProcessCommand;
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

    public CompleteRegistrationProcessCommand toCommand() {
        return new CompleteRegistrationProcessCommand(firstConnectionToken, toUpdatePassword());
    }
}
