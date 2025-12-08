package com.langa.backend.domain.users.services;

import com.langa.backend.domain.users.User;

public interface TokenProvider {
    String generateToken(User user);
    String generateToken(String user);
    String getUsernameFromToken(String token);

    boolean validateToken(String token);
}
