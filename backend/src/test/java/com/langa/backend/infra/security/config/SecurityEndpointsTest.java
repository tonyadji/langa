package com.langa.backend.infra.security.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SecurityEndpointsTest {

    @Test
    void getUnsecured_shouldSplitCommaSeparatedPatterns() {
        SecurityEndpoints endpoints = new SecurityEndpoints();
        endpoints.setUnsecured("/api/auth/**,/api/ingestion/**,/swagger-ui.html");

        assertArrayEquals(new String[]{"/api/auth/**", "/api/ingestion/**", "/swagger-ui.html"}, endpoints.getUnsecured());
    }
}
