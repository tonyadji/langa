package com.langa.backend.domain.users.valueobjects;

import com.langa.backend.common.utils.KeyGenerator;

import java.time.Instant;

public record FirstConnectionToken(
        String token,
        Instant generatedAt,
        Instant expiresAt
) {

    public static FirstConnectionToken newToken(String accountKey, String email) {
        return new FirstConnectionToken(
                KeyGenerator.genericToken(accountKey, email),
                Instant.now(),
                Instant.now().plusSeconds(86400)
        );
    }
}
