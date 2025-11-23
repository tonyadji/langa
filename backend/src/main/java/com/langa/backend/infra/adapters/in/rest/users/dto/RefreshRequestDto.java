package com.langa.backend.infra.adapters.in.rest.users.dto;

import com.langa.backend.domain.users.usecases.refreshaccesstoken.RefreshAccessTokenCommand;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDto(
        @NotBlank String refreshToken) {
    public RefreshAccessTokenCommand toCommand() {
        return new RefreshAccessTokenCommand(refreshToken);
    }
}
