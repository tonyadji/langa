package com.langa.backend.infra.security.auth;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.services.UserService;
import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import com.langa.backend.domain.users.valueobjects.UserId;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import com.langa.backend.infra.security.identity.ExternalIdentityResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalJwtAuthenticationConverterTest {

    @Mock
    private UserService userService;
    @Mock
    private ExternalIdentityResolver identityResolver;

    @InjectMocks
    private ExternalJwtAuthenticationConverter converter;

    private final Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .claim("sub", "sub-1")
            .build();

    private final User user = User.populate(UserId.of("id-1", "User@Example.com", "acc-1"), "cognito", "sub-1", UserStatus.ACTIVE);

    @Test
    void convert_shouldAuthenticateKnownUserWithoutResolvingFullIdentity() {
        when(identityResolver.provider()).thenReturn("cognito");
        when(identityResolver.subject(jwt)).thenReturn("sub-1");
        when(userService.findByExternalIdentity("cognito", "sub-1")).thenReturn(Optional.of(user));

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertTrue(authentication.isAuthenticated());
        assertEquals("User@Example.com", ((UserDetails) authentication.getPrincipal()).getUsername());
        assertSame(jwt, authentication.getCredentials());
        verify(identityResolver, never()).resolve(any());
        verify(userService, never()).provisionExternalUser(any());
    }

    @Test
    void convert_shouldProvisionUnknownUser() {
        ExternalIdentity identity = new ExternalIdentity("cognito", "sub-1", "user@example.com");
        when(identityResolver.provider()).thenReturn("cognito");
        when(identityResolver.subject(jwt)).thenReturn("sub-1");
        when(userService.findByExternalIdentity("cognito", "sub-1")).thenReturn(Optional.empty());
        when(identityResolver.resolve(jwt)).thenReturn(identity);
        when(userService.provisionExternalUser(identity)).thenReturn(user);

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertEquals("User@Example.com", ((UserDetails) authentication.getPrincipal()).getUsername());
    }

    @Test
    void convert_shouldPropagateInvalidToken_whenSubjectMissing() {
        when(identityResolver.subject(jwt)).thenThrow(new InvalidBearerTokenException("missing sub"));

        assertThrows(InvalidBearerTokenException.class, () -> converter.convert(jwt));
        verifyNoInteractions(userService);
    }

    @Test
    void convert_shouldRejectToken_whenProvisioningFails() {
        when(identityResolver.provider()).thenReturn("cognito");
        when(identityResolver.subject(jwt)).thenReturn("sub-1");
        when(userService.findByExternalIdentity("cognito", "sub-1")).thenReturn(Optional.empty());
        when(identityResolver.resolve(jwt)).thenReturn(new ExternalIdentity("cognito", "sub-1", "user@example.com"));
        when(userService.provisionExternalUser(any())).thenThrow(new IllegalStateException("boom"));

        assertThrows(InvalidBearerTokenException.class, () -> converter.convert(jwt));
    }
}
