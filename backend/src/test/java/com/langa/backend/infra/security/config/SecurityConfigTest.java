package com.langa.backend.infra.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private static final String ISSUER = "https://tenant-id.ciamlogin.com/tenant-id/v2.0";
    private static final String API_CLIENT_ID = "api-client-id";

    private OAuth2TokenValidator<Jwt> validator;

    @BeforeEach
    void setUp() {
        EntraProperties properties = new EntraProperties();
        properties.setIssuerUri(ISSUER);
        properties.setJwkSetUri("https://langa.ciamlogin.com/tenant-id/discovery/v2.0/keys");
        properties.setAudiences(API_CLIENT_ID + ", api://" + API_CLIENT_ID);
        properties.setRequiredScope("access_as_user");
        validator = SecurityConfig.jwtValidator(properties);
    }

    private static Jwt.Builder validToken() {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer(ISSUER)
                .audience(List.of(API_CLIENT_ID))
                .claim("scp", "openid access_as_user")
                .issuedAt(Instant.now().minusSeconds(10))
                .expiresAt(Instant.now().plusSeconds(300));
    }

    @Test
    void entraProperties_shouldSplitAudiences() {
        EntraProperties properties = new EntraProperties();
        properties.setAudiences("a, b,,c");

        assertEquals(List.of("a", "b", "c"), properties.getAudiences());
    }

    @Test
    void validator_shouldAcceptValidToken() {
        assertFalse(validator.validate(validToken().build()).hasErrors());
    }

    @Test
    void validator_shouldAcceptAppIdUriAudience() {
        assertFalse(validator.validate(validToken().audience(List.of("api://" + API_CLIENT_ID)).build()).hasErrors());
    }

    @Test
    void validator_shouldRejectOtherIssuer() {
        Jwt token = validToken().issuer("https://login.microsoftonline.com/other-tenant/v2.0").build();

        assertTrue(validator.validate(token).hasErrors());
    }

    @Test
    void validator_shouldRejectOtherAudience() {
        Jwt token = validToken().audience(List.of("another-api")).build();

        assertTrue(validator.validate(token).hasErrors());
    }

    @Test
    void validator_shouldRejectMissingScope() {
        Jwt token = validToken().claim("scp", "openid profile").build();

        assertTrue(validator.validate(token).hasErrors());
    }

    @Test
    void validator_shouldRejectExpiredToken() {
        Jwt token = validToken()
                .issuedAt(Instant.now().minusSeconds(7200))
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();

        assertTrue(validator.validate(token).hasErrors());
    }
}
