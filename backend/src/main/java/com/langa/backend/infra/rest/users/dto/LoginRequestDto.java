package com.langa.backend.infra.rest.users.dto;

import com.langa.backend.domain.users.valueobjects.AuthRequest;

public record LoginRequestDto(String username, String password) {
    public AuthRequest toAuthRequest() {
        return new AuthRequest(username, password);
    }
}
