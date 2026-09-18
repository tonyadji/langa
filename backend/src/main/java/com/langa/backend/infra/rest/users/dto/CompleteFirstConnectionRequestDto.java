package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.usecases.completefirstconnection.CompleteFirstConnectionCommand;
import com.langa.backend.domain.users.valueobjects.UpdatePassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompleteFirstConnectionRequestDto(
        @NotBlank @NotNull String firstConnectionToken,
        @NotBlank @NotNull String password,
        @NotBlank @NotNull String confirmationPassword
) {
    public CompleteFirstConnectionCommand toCommand() {
        return new CompleteFirstConnectionCommand(firstConnectionToken, new UpdatePassword(password, confirmationPassword));
    }
}
