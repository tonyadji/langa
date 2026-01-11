package com.langa.backend.domain.users.services;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import com.langa.backend.domain.users.repositories.TokenRepository;
import com.langa.backend.domain.users.valueobjects.Token;
import com.langa.backend.domain.users.valueobjects.TokenType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TokenService {

    private final TokenRepository repository;
    private final long expirationInDays;
    private final TokenProvider tokenProvider;

    public TokenService(TokenRepository repository, long expirationInDays, TokenProvider tokenProvider) {
        this.repository = repository;
        this.expirationInDays = expirationInDays;
        this.tokenProvider = tokenProvider;
    }

    public Token issue(TokenType type, String userEmail) {
        Instant exp = Instant.now().plus(expirationInDays, ChronoUnit.DAYS);
        Token token = new Token(tokenProvider.generateToken(userEmail,type), userEmail, exp, type);
        return repository.save(token);
    }

    public String validateAndGetUserEmail(String token) {
        Token foundToken = repository.findByToken(token).orElseThrow(() ->
                new GenericException("Refresh token not found", null, Errors.INVALID_CREDENTIALS));
        if (foundToken.isRevoked() || foundToken.isExpired()) {
            throw new GenericException("Refresh token invalid or expired", null, Errors.INVALID_CREDENTIALS);
        }
        return foundToken.getBearer();
    }

    public void rotate(String oldToken) {
        repository.revokeByToken(oldToken);
    }

    public void revokeAllByUserEmail(String userEmail) {
        repository.revokeAllByUserEmail(userEmail);
    }
}
