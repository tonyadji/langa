package com.langa.backend.infra.rest.users.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String confirmationPassword) {
}
