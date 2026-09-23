package com.langa.backend.infra.security.auth;

import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.services.UserService;
import com.langa.backend.domain.users.valueobjects.UserId;
import com.langa.backend.domain.users.valueobjects.UserStatus;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntraJwtAuthenticationConverterTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private EntraJwtAuthenticationConverter converter;

    private static Jwt.Builder jwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
    }

    @Test
    void convert_shouldAuthenticateWithLocalUserEmailAsUsername() {
        User user = User.populate(UserId.of("id-1", "User@Example.com", "acc-1"), "oid-1", UserStatus.ACTIVE);
        when(userService.provisionExternalUser("oid-1", "user@example.com")).thenReturn(user);
        Jwt token = jwt().claim("oid", "oid-1").claim("email", " user@example.com ").build();

        AbstractAuthenticationToken authentication = converter.convert(token);

        assertTrue(authentication.isAuthenticated());
        assertEquals("User@Example.com", ((UserDetails) authentication.getPrincipal()).getUsername());
        assertSame(token, authentication.getCredentials());
    }

    @Test
    void convert_shouldReject_whenEmailClaimMissing() {
        Jwt token = jwt().claim("oid", "oid-1").build();

        assertThrows(InvalidBearerTokenException.class, () -> converter.convert(token));
        verifyNoInteractions(userService);
    }

    @Test
    void convert_shouldReject_whenOidClaimMissing() {
        Jwt token = jwt().claim("email", "user@example.com").build();

        assertThrows(InvalidBearerTokenException.class, () -> converter.convert(token));
        verifyNoInteractions(userService);
    }

    @Test
    void convert_shouldReject_whenProvisioningFails() {
        when(userService.provisionExternalUser("oid-1", "user@example.com")).thenThrow(new IllegalStateException("boom"));
        Jwt token = jwt().claim("oid", "oid-1").claim("email", "user@example.com").build();

        assertThrows(InvalidBearerTokenException.class, () -> converter.convert(token));
    }
}
