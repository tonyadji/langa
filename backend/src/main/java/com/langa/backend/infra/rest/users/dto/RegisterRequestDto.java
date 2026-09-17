package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.usecases.register.RegisterUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequestDto(
        @NotBlank @Email String username,
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,128}$",
                message = "Password must be 8-128 characters long and contain at least one letter and one digit")
        String password,
        @NotBlank String confirmationPassword) {

    public RegisterUserCommand toCommand() {
        return new RegisterUserCommand(username, password, confirmationPassword);
    }
}
