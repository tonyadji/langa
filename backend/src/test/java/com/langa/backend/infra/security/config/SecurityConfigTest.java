package com.langa.backend.infra.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private static final String ENTRA_ISSUER = "https://tenant-id.ciamlogin.com/tenant-id/v2.0";
    private static final String API_CLIENT_ID = "api-client-id";
    private static final String COGNITO_ISSUER = "https://cognito-idp.eu-west-3.amazonaws.com/eu-west-3_pool";
    private static final String COGNITO_CLIENT_ID = "cognito-app-client-id";

    private static AuthProviderProperties entraProperties() {
        AuthProviderProperties properties = new AuthProviderProperties();
        properties.setProvider("entra");
        properties.setIssuerUri(ENTRA_ISSUER);
        properties.setJwkSetUri("https://your-tenant.ciamlogin.com/tenant-id/discovery/v2.0/keys");
        properties.setAudiences(API_CLIENT_ID + ", api://" + API_CLIENT_ID);
        properties.setScopeClaim("scp");
        properties.setRequiredScope("access_as_user");
        properties.setSubjectClaim("oid");
        return properties;
    }

    private static AuthProviderProperties cognitoProperties() {
        AuthProviderProperties properties = new AuthProviderProperties();
        properties.setProvider("cognito");
        properties.setIssuerUri(COGNITO_ISSUER);
        properties.setJwkSetUri(COGNITO_ISSUER + "/.well-known/jwks.json");
        properties.setAudienceClaim("client_id");
        properties.setAudiences(COGNITO_CLIENT_ID);
        properties.setRequiredScope("langa-api/access");
        properties.setRequiredClaims("token_use=access");
        properties.setEmailSource(AuthProviderProperties.EmailSource.USERINFO);
        return properties;
    }

    private static Jwt.Builder entraToken() {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer(ENTRA_ISSUER)
                .audience(List.of(API_CLIENT_ID))
                .claim("scp", "openid access_as_user")
                .issuedAt(Instant.now().minusSeconds(10))
                .expiresAt(Instant.now().plusSeconds(300));
    }

    private static Jwt.Builder cognitoToken() {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuer(COGNITO_ISSUER)
                .claim("client_id", COGNITO_CLIENT_ID)
                .claim("scope", "openid langa-api/access")
                .claim("token_use", "access")
                .issuedAt(Instant.now().minusSeconds(10))
                .expiresAt(Instant.now().plusSeconds(300));
    }

    @Test
    void properties_shouldSplitAudiencesAndRequiredClaims() {
        AuthProviderProperties properties = new AuthProviderProperties();
        properties.setAudiences("a, b,,c");
        properties.setRequiredClaims("token_use=access, tenant = t1");

        assertEquals(List.of("a", "b", "c"), properties.getAudiences());
        assertEquals(Map.of("token_use", "access", "tenant", "t1"), properties.getRequiredClaims());
    }

    @Test
    void properties_shouldRejectMalformedRequiredClaims() {
        AuthProviderProperties properties = new AuthProviderProperties();
        properties.setRequiredClaims("token_use");

        assertThrows(IllegalArgumentException.class, properties::getRequiredClaims);
    }

    @Test
    void properties_shouldUseOidcDefaults() {
        AuthProviderProperties properties = new AuthProviderProperties();

        assertEquals("aud", properties.getAudienceClaim());
        assertEquals("scope", properties.getScopeClaim());
        assertEquals("sub", properties.getSubjectClaim());
        assertEquals("email", properties.getEmailClaim());
        assertEquals(AuthProviderProperties.EmailSource.CLAIM, properties.getEmailSource());
        assertTrue(properties.getRequiredClaims().isEmpty());
    }

    @Test
    void validator_shouldRequireAudiences() {
        AuthProviderProperties properties = entraProperties();
        properties.setAudiences(" ");

        assertThrows(IllegalStateException.class, () -> SecurityConfig.jwtValidator(properties));
    }

    @Test
    void entraValidator_shouldAcceptValidToken() {
        assertFalse(SecurityConfig.jwtValidator(entraProperties()).validate(entraToken().build()).hasErrors());
    }

    @Test
    void entraValidator_shouldAcceptAppIdUriAudience() {
        Jwt token = entraToken().audience(List.of("api://" + API_CLIENT_ID)).build();

        assertFalse(SecurityConfig.jwtValidator(entraProperties()).validate(token).hasErrors());
    }

    @Test
    void entraValidator_shouldRejectOtherIssuer() {
        Jwt token = entraToken().issuer("https://login.microsoftonline.com/other-tenant/v2.0").build();

        assertTrue(SecurityConfig.jwtValidator(entraProperties()).validate(token).hasErrors());
    }

    @Test
    void entraValidator_shouldRejectOtherAudience() {
        Jwt token = entraToken().audience(List.of("another-api")).build();

        assertTrue(SecurityConfig.jwtValidator(entraProperties()).validate(token).hasErrors());
    }

    @Test
    void entraValidator_shouldRejectMissingScope() {
        Jwt token = entraToken().claim("scp", "openid profile").build();

        assertTrue(SecurityConfig.jwtValidator(entraProperties()).validate(token).hasErrors());
    }

    @Test
    void entraValidator_shouldRejectExpiredToken() {
        Jwt token = entraToken()
                .issuedAt(Instant.now().minusSeconds(7200))
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();

        assertTrue(SecurityConfig.jwtValidator(entraProperties()).validate(token).hasErrors());
    }

    @Test
    void validator_shouldSkipScopeCheck_whenNoScopeRequired() {
        AuthProviderProperties properties = entraProperties();
        properties.setRequiredScope("");
        Jwt token = entraToken().claim("scp", "openid").build();

        assertFalse(SecurityConfig.jwtValidator(properties).validate(token).hasErrors());
    }

    @Test
    void cognitoValidator_shouldAcceptValidAccessToken() {
        assertFalse(SecurityConfig.jwtValidator(cognitoProperties()).validate(cognitoToken().build()).hasErrors());
    }

    @Test
    void cognitoValidator_shouldRejectIdToken() {
        Jwt token = cognitoToken().claim("token_use", "id").build();

        assertTrue(SecurityConfig.jwtValidator(cognitoProperties()).validate(token).hasErrors());
    }

    @Test
    void cognitoValidator_shouldRejectOtherClient() {
        Jwt token = cognitoToken().claim("client_id", "other-client").build();

        assertTrue(SecurityConfig.jwtValidator(cognitoProperties()).validate(token).hasErrors());
    }

    @Test
    void validator_shouldAcceptScopesAsList() {
        Jwt token = cognitoToken().claim("scope", List.of("openid", "langa-api/access")).build();

        assertFalse(SecurityConfig.jwtValidator(cognitoProperties()).validate(token).hasErrors());
    }
}
