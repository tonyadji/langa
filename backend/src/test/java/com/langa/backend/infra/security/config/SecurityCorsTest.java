package com.langa.backend.infra.security.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityCorsTest {

    @Test
    void shouldSplitCommaSeparatedValues() {
        SecurityCors cors = new SecurityCors();
        cors.setAllowedOrigins("http://localhost:3000,http://localhost:5173");
        cors.setAllowedMethods("GET,POST,PUT");
        cors.setAllowedHeaders("Authorization,Content-Type");
        cors.setAllowCredentials(true);
        cors.setPatternRegistry("/**");

        assertEquals(2, cors.getAllowedOrigins().size());
        assertTrue(cors.getAllowedOrigins().contains("http://localhost:3000"));
        assertEquals(3, cors.getAllowedMethods().size());
        assertEquals(2, cors.getAllowedHeaders().size());
        assertTrue(cors.isAllowCredentials());
        assertEquals("/**", cors.getPatternRegistry());
    }

    @Test
    void shouldReturnSingleValue_whenNoCommaPresent() {
        SecurityCors cors = new SecurityCors();
        cors.setAllowedOrigins("http://localhost:3000");
        cors.setAllowCredentials(false);

        assertEquals(1, cors.getAllowedOrigins().size());
        assertFalse(cors.isAllowCredentials());
    }
}
