package com.langa.backend.domain.users.repositories;

import com.langa.backend.domain.users.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
    void revokeByToken(String token);
    void revokeAllByUserEmail(String userEmail);
}
