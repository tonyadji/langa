package com.langa.backend.infra.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenID Connect / OAuth2 identity provider issuing the access tokens accepted by the API.
 * Defaults follow the OIDC conventions; see the README for Entra ID and Cognito examples.
 */
@Component
@ConfigurationProperties(prefix = "application.security.auth")
@Getter
@Setter
public class AuthProviderProperties {

    public enum EmailSource {
        /** The email is a claim of the access token. */
        CLAIM,
        /** The email is fetched from the provider UserInfo endpoint (e.g. Cognito access tokens have no email). */
        USERINFO
    }

    /** Provider name, stored with the users it authenticates (e.g. entra, cognito). */
    private String provider;
    /** Expected {@code iss} claim. */
    private String issuerUri;
    /** Signing keys endpoint. */
    private String jwkSetUri;

    /** Claim holding the audience (Cognito access tokens use {@code client_id}). */
    private String audienceClaim = "aud";
    /** Accepted audience values, comma separated. */
    @Getter(lombok.AccessLevel.NONE)
    private String audiences;

    /** Claim holding the granted scopes (Entra ID uses {@code scp}). */
    private String scopeClaim = "scope";
    /** Scope the access token must contain; no check when blank. */
    private String requiredScope;

    /** Other claims the access token must contain, as {@code name=value} pairs separated by commas. */
    @Getter(lombok.AccessLevel.NONE)
    private String requiredClaims;

    /** Claim holding the stable user identifier (Entra ID uses {@code oid}). */
    private String subjectClaim = "sub";
    private EmailSource emailSource = EmailSource.CLAIM;
    private String emailClaim = "email";
    /** UserInfo endpoint, required when {@code email-source} is {@code userinfo}. */
    private String userinfoUri;

    public List<String> getAudiences() {
        return split(audiences).toList();
    }

    public Map<String, String> getRequiredClaims() {
        Map<String, String> claims = new LinkedHashMap<>();
        split(requiredClaims).forEach(pair -> {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length != 2 || keyValue[0].isBlank()) {
                throw new IllegalArgumentException("Invalid required claim (expected name=value): " + pair);
            }
            claims.put(keyValue[0].trim(), keyValue[1].trim());
        });
        return claims;
    }

    private static java.util.stream.Stream<String> split(String value) {
        if (value == null) {
            return java.util.stream.Stream.empty();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty());
    }
}
