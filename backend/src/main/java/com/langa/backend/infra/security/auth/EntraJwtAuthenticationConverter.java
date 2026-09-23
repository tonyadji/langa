package com.langa.backend.infra.security.auth;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

/**
 * Turns a validated Entra ID access token into the application principal.
 * The principal is the local user (provisioned on first sign-in) so controllers keep using
 * {@code @AuthenticationPrincipal UserDetails} with the local email as username.
 */
@Component
@Slf4j
public class EntraJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    static final String OBJECT_ID_CLAIM = "oid";
    static final String EMAIL_CLAIM = "email";

    private final UserService userService;

    public EntraJwtAuthenticationConverter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        final String externalId = jwt.getClaimAsString(OBJECT_ID_CLAIM);
        final String email = jwt.getClaimAsString(EMAIL_CLAIM);
        if (isBlank(externalId) || isBlank(email)) {
            log.warn("Access token without {} or {} claim (check the optional claims of the API app registration)",
                    OBJECT_ID_CLAIM, EMAIL_CLAIM);
            throw new InvalidBearerTokenException("Access token is missing required claims");
        }

        final User user;
        try {
            user = userService.provisionExternalUser(externalId, email.trim());
        } catch (RuntimeException e) {
            log.error("Unable to provision user for external identity {}", externalId, e);
            throw new InvalidBearerTokenException("Unable to resolve user from access token", e);
        }

        final UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("")
                .authorities(new String[0])
                .build();
        return UsernamePasswordAuthenticationToken.authenticated(principal, jwt, principal.getAuthorities());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
