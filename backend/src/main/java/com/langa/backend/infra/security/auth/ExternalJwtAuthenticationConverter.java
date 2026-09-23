package com.langa.backend.infra.security.auth;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.services.UserService;
import com.langa.backend.infra.security.identity.ExternalIdentityResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

/**
 * Turns a validated access token, whatever the identity provider, into the application principal.
 * The principal is the local user (provisioned on first sign-in) so controllers keep using
 * {@code @AuthenticationPrincipal UserDetails} with the local email as username.
 */
@Component
@Slf4j
public class ExternalJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;
    private final ExternalIdentityResolver identityResolver;

    public ExternalJwtAuthenticationConverter(UserService userService, ExternalIdentityResolver identityResolver) {
        this.userService = userService;
        this.identityResolver = identityResolver;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        final String subject = identityResolver.subject(jwt);

        final User user;
        try {
            // The full identity (which may require a call to the provider) is only needed on first sign-in
            user = userService.findByExternalIdentity(identityResolver.provider(), subject)
                    .orElseGet(() -> userService.provisionExternalUser(identityResolver.resolve(jwt)));
        } catch (InvalidBearerTokenException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Unable to provision user for {} identity {}", identityResolver.provider(), subject, e);
            throw new InvalidBearerTokenException("Unable to resolve user from access token", e);
        }

        final UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("")
                .authorities(new String[0])
                .build();
        return UsernamePasswordAuthenticationToken.authenticated(principal, jwt, principal.getAuthorities());
    }
}
