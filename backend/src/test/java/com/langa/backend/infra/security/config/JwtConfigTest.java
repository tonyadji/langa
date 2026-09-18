package com.langa.backend.infra.security.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtConfigTest {

    private static final String SECRET = "a-secret-key-that-is-at-least-32-bytes-long-for-hs256";

    @Test
    void shouldExposeKidAndExpiration() {
        JwtConfig config = new JwtConfig(SECRET, "kid-1", 3600000L);

        assertEquals("kid-1", config.getKid());
        assertEquals(3600000L, config.getExpirationMs());
        assertNotNull(config.getKey());
    }

    @Test
    void getKey_shouldBeStableForSameSecret() {
        JwtConfig config1 = new JwtConfig(SECRET, "kid-1", 3600000L);
        JwtConfig config2 = new JwtConfig(SECRET, "kid-1", 3600000L);

        assertEquals(config1.getKey(), config2.getKey());
    }
}
