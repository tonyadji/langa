package com.langa.backend.domain.users.usecases.refreshaccesstoken;

public record RefreshAccessTokenCommand(
        String refreshToken
) {
    public void validate() {
    }
}
