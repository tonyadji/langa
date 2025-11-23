package com.langa.backend.domain.users.services;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.RefreshToken;
import com.langa.backend.domain.users.exceptions.TokenException;
import com.langa.backend.domain.users.repositories.RefreshTokenRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long expirationInDays;

    public RefreshTokenService(RefreshTokenRepository repository, long expirationInDays) {
        this.repository = repository;
        this.expirationInDays = expirationInDays;
    }

    public RefreshToken issue(String userEmail) {
        String token = randomToken();
        Instant exp = Instant.now().plus(expirationInDays, ChronoUnit.DAYS);
        RefreshToken rt = new RefreshToken(token, userEmail, exp);
        return repository.save(rt);
    }

    public String validateAndGetUserEmail(String token) {
        RefreshToken rt = repository.findByToken(token).orElseThrow(() ->
                new TokenException("Refresh token not found", null, Errors.INVALID_CREDENTIALS));
        if (rt.isRevoked() || rt.isExpired()) {
            throw new TokenException("Refresh token invalid or expired", null, Errors.INVALID_CREDENTIALS);
        }
        return rt.getUserEmail();
    }

    public void rotate(String oldToken) {
        repository.revokeByToken(oldToken);
    }

    private String randomToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
