package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.usecases.refreshtoken.RefreshAccessTokenCommand;

public record RefreshRequestDto(String refreshToken) {
    public RefreshAccessTokenCommand toCommand() {
        return new RefreshAccessTokenCommand(refreshToken);
    }
}
