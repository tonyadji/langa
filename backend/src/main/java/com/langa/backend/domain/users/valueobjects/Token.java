package com.langa.backend.domain.users.valueobjects;

import lombok.Getter;

import java.time.Instant;

@Getter
public class Token {
    private final String value;
    private final String bearer;
    private final Instant expiresAt;
    private boolean revoked;
    private final TokenType type;

    public Token(String value, String bearer, Instant expiresAt, TokenType type) {
        this.value = value;
        this.bearer = bearer;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.type = type;
    }

    public Token(String value, String bearer, Instant expiresAt, boolean revoked, TokenType type) {
        this.value = value;
        this.bearer = bearer;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.type = type;
    }

    public void revoke() { this.revoked = true; }
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }

}
