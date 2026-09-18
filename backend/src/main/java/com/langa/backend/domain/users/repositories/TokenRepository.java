package com.langa.backend.domain.users.repositories;

import com.langa.backend.domain.users.valueobjects.Token;

import java.util.Optional;

public interface TokenRepository {
    void revokeByToken(String token);
    void revokeAllByUserEmail(String userEmail);
    Token save(Token token);
    Optional<Token> findByToken(String token);

    boolean isRevoked(String token);
}
