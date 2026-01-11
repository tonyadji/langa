package com.langa.backend.domain.users.services;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.valueobjects.TokenType;

public interface TokenProvider {
    String generateToken(User user, TokenType tokenType);
    String generateToken(String user, TokenType tokenType);
    String getUsernameFromToken(String token);

    boolean validateToken(String token);

    String getClaim(String claim);
}
