package com.langa.backend.infra.security.identity;

import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import com.langa.backend.infra.security.config.AuthProviderProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

/**
 * Reads the subject and the email from access token claims (Entra ID, Keycloak, Auth0...).
 */
public class ClaimsExternalIdentityResolver implements ExternalIdentityResolver {

    private final AuthProviderProperties properties;

    public ClaimsExternalIdentityResolver(AuthProviderProperties properties) {
        this.properties = properties;
    }

    @Override
    public String provider() {
        return properties.getProvider();
    }

    @Override
    public String subject(Jwt jwt) {
        return requiredClaim(jwt, properties.getSubjectClaim());
    }

    @Override
    public ExternalIdentity resolve(Jwt jwt) {
        return new ExternalIdentity(provider(), subject(jwt), email(jwt));
    }

    protected String email(Jwt jwt) {
        return requiredClaim(jwt, properties.getEmailClaim());
    }

    static String requiredClaim(Jwt jwt, String claim) {
        final String value = jwt.getClaimAsString(claim);
        if (value == null || value.isBlank()) {
            throw new InvalidBearerTokenException("Access token is missing the '" + claim + "' claim");
        }
        return value;
    }
}
