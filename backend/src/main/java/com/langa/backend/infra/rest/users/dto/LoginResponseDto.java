package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.valueobjects.AuthTokens;

public record LoginResponseDto(String accessToken, String refreshToken) {
    public static LoginResponseDto of(AuthTokens tokens) {
        return new LoginResponseDto(tokens.accessToken(), tokens.refreshToken());
    }
}
