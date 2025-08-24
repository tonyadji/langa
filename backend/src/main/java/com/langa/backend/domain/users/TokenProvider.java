package com.langa.backend.domain.users;

public interface TokenProvider {
    String generateToken(User user);
    String getUsernameFromToken(String token);

    boolean validateToken(String token);
}
