package com.langa.backend.domain.users;

import lombok.Getter;

import java.time.Instant;

@Getter

public class RefreshToken {
    private String id;
    private String token;
    private String userEmail;
    private Instant expiresAt;
    private boolean revoked;

    public RefreshToken() {}

    public RefreshToken(String token, String userEmail, Instant expiresAt) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    public RefreshToken(String token, String userEmail, Instant expiresAt, boolean revoked) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public void revoke() { this.revoked = true; }
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
}
