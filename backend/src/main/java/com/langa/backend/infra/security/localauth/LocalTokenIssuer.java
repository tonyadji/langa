package com.langa.backend.infra.security.localauth;

import com.langa.backend.infra.security.config.AuthProviderProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Issues RS256 access tokens for the local authentication mode.
 * The claims follow the configured identity provider settings ({@link AuthProviderProperties}), so the tokens
 * pass the same validation as the provider tokens and users are provisioned the same way.
 */
public class LocalTokenIssuer {

    private final AuthProviderProperties authProperties;
    private final Duration tokenTtl;
    private final RSAPublicKey publicKey;
    private final String keyId;
    private final JwtEncoder encoder;

    public LocalTokenIssuer(AuthProviderProperties authProperties, Duration tokenTtl, KeyPair keyPair) {
        this.authProperties = authProperties;
        this.tokenTtl = tokenTtl;
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.keyId = UUID.randomUUID().toString();
        final RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .keyID(keyId)
                .build();
        this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    /** New RSA key pair; kept in memory only, so tokens are invalidated when the backend restarts. */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA is not available", e);
        }
    }

    public RSAPublicKey publicKey() {
        return publicKey;
    }

    public IssuedToken issue(String email) {
        final String normalizedEmail = LocalIdentity.normalizeEmail(email);
        final String subject = LocalIdentity.subjectFor(normalizedEmail);
        final Instant now = Instant.now();

        final JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(authProperties.getIssuerUri())
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(tokenTtl))
                .claim(authProperties.getAudienceClaim(), authProperties.getAudiences())
                .claim(authProperties.getSubjectClaim(), subject)
                .claim(authProperties.getEmailClaim(), normalizedEmail);
        final String requiredScope = authProperties.getRequiredScope();
        if (requiredScope != null && !requiredScope.isBlank()) {
            claims.claim(authProperties.getScopeClaim(), requiredScope);
        }
        authProperties.getRequiredClaims().forEach(claims::claim);

        final JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(keyId).build();
        final String token = encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
        return new IssuedToken(token, "Bearer", tokenTtl.toSeconds());
    }

    public record IssuedToken(String accessToken, String tokenType, long expiresIn) {
    }
}
