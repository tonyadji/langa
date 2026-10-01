package com.langa.backend.infra.security.localauth;

import com.langa.backend.infra.security.config.AuthProviderProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocalTokenIssuerTest {

    static AuthProviderProperties localProperties() {
        AuthProviderProperties properties = new AuthProviderProperties();
        properties.setProvider(LocalIdentity.PROVIDER);
        properties.setIssuerUri("langa-local");
        properties.setAudiences("langa-local");
        properties.setScopeClaim("scp");
        properties.setRequiredScope("access_as_user");
        properties.setSubjectClaim("oid");
        properties.setRequiredClaims("token_use=access");
        return properties;
    }

    private final LocalTokenIssuer issuer =
            new LocalTokenIssuer(localProperties(), Duration.ofHours(1), LocalTokenIssuer.generateKeyPair());

    private Jwt decode(String token) {
        return NimbusJwtDecoder.withPublicKey(issuer.publicKey()).build().decode(token);
    }

    @Test
    void issue_shouldSignTokenWithTheConfiguredClaims() {
        LocalTokenIssuer.IssuedToken token = issuer.issue(" Demo@Langa.Local ");

        Jwt jwt = decode(token.accessToken());
        String subject = LocalIdentity.subjectFor("demo@langa.local");
        assertEquals("langa-local", jwt.getClaimAsString("iss"));
        assertEquals(List.of("langa-local"), jwt.getAudience());
        assertEquals("access_as_user", jwt.getClaimAsString("scp"));
        assertEquals(subject, jwt.getSubject());
        assertEquals(subject, jwt.getClaimAsString("oid"));
        assertEquals("demo@langa.local", jwt.getClaimAsString("email"));
        assertEquals("access", jwt.getClaimAsString("token_use"));
        assertEquals("RS256", jwt.getHeaders().get("alg"));
        assertEquals(Duration.ofHours(1), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
        assertEquals("Bearer", token.tokenType());
        assertEquals(3600, token.expiresIn());
    }

    @Test
    void issue_shouldOmitScope_whenNoScopeRequired() {
        AuthProviderProperties properties = localProperties();
        properties.setRequiredScope(" ");
        LocalTokenIssuer noScopeIssuer =
                new LocalTokenIssuer(properties, Duration.ofMinutes(5), LocalTokenIssuer.generateKeyPair());

        Jwt jwt = NimbusJwtDecoder.withPublicKey(noScopeIssuer.publicKey()).build()
                .decode(noScopeIssuer.issue("user@example.com").accessToken());

        assertNull(jwt.getClaim("scp"));
        assertTrue(jwt.getExpiresAt().isBefore(jwt.getIssuedAt().plus(6, ChronoUnit.MINUTES)));
    }

    @Test
    void token_shouldBeRejectedByAnotherKey() {
        String token = issuer.issue("user@example.com").accessToken();
        LocalTokenIssuer otherIssuer =
                new LocalTokenIssuer(localProperties(), Duration.ofHours(1), LocalTokenIssuer.generateKeyPair());

        NimbusJwtDecoder otherDecoder = NimbusJwtDecoder.withPublicKey(otherIssuer.publicKey()).build();
        assertThrows(JwtException.class, () -> otherDecoder.decode(token));
    }

    @Test
    void subject_shouldBeStablePerEmail() {
        assertEquals(LocalIdentity.subjectFor("user@example.com"), LocalIdentity.subjectFor(" USER@example.com"));
        assertNotEquals(LocalIdentity.subjectFor("user@example.com"), LocalIdentity.subjectFor("other@example.com"));
    }
}
