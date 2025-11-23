package com.langa.backend.infra.adapters.in.rest.users.dto;

import com.langa.backend.domain.users.usecases.register.RegisterCommand;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String confirmationPassword) {

    public RegisterCommand toCommand() {
        return new RegisterCommand(username, password, confirmationPassword);
    }
}
