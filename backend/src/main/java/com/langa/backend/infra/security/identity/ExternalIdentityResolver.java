package com.langa.backend.infra.security.identity;

import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Extracts the identity of the user from a validated access token, whatever the identity provider.
 */
public interface ExternalIdentityResolver {

    /** Name of the identity provider. */
    String provider();

    /** Stable identifier of the user; cheap, read from the token. */
    String subject(Jwt jwt);

    /**
     * Full identity including the email; may call the identity provider,
     * so it is only used when the user is not known yet.
     */
    ExternalIdentity resolve(Jwt jwt);
}
