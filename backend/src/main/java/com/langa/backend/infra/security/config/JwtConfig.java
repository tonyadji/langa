package com.langa.backend.infra.security.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtConfig {

    private final SecretKey key;
    private final String kid;
    private final long expirationMs;

    public JwtConfig(
            @Value("${application.security.jwt.secret}") String secret,
            @Value("${application.security.jwt.kid}") String kid,
            @Value("${application.security.jwt.expiration}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.kid = kid;
        this.expirationMs = expirationMs;
    }

    public SecretKey getKey() {
        return key;
    }

    public String getKid() {
        return kid;
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
