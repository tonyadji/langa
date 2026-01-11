package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.usecases.register.RegisterUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(
        @NotBlank @Email String username,
        @NotBlank String password,
        @NotBlank String confirmationPassword) {

    public RegisterUserCommand toCommand() {
        return new RegisterUserCommand(username, password, confirmationPassword);
    }
}
