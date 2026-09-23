package com.langa.backend.infra.security.identity;

import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import com.langa.backend.infra.security.config.AuthProviderProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExternalIdentityResolverTest {

    private static final String USERINFO_URI = "https://langa.auth.eu-west-3.amazoncognito.com/oauth2/userInfo";

    private static Jwt.Builder token() {
        return Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
    }

    private static AuthProviderProperties entraProperties() {
        AuthProviderProperties properties = new AuthProviderProperties();
        properties.setProvider("entra");
        properties.setSubjectClaim("oid");
        return properties;
    }

    private static AuthProviderProperties cognitoProperties() {
        AuthProviderProperties properties = new AuthProviderProperties();
        properties.setProvider("cognito");
        properties.setEmailSource(AuthProviderProperties.EmailSource.USERINFO);
        properties.setUserinfoUri(USERINFO_URI);
        return properties;
    }

    @Test
    void claimsResolver_shouldReadConfiguredClaims() {
        ClaimsExternalIdentityResolver resolver = new ClaimsExternalIdentityResolver(entraProperties());
        Jwt jwt = token().subject("pairwise-sub").claim("oid", "oid-1").claim("email", "user@example.com").build();

        assertEquals("entra", resolver.provider());
        assertEquals("oid-1", resolver.subject(jwt));
        assertEquals(new ExternalIdentity("entra", "oid-1", "user@example.com"), resolver.resolve(jwt));
    }

    @Test
    void claimsResolver_shouldRejectTokenWithoutSubject() {
        ClaimsExternalIdentityResolver resolver = new ClaimsExternalIdentityResolver(entraProperties());
        Jwt jwt = token().claim("email", "user@example.com").build();

        assertThrows(InvalidBearerTokenException.class, () -> resolver.subject(jwt));
    }

    @Test
    void claimsResolver_shouldRejectTokenWithoutEmail() {
        ClaimsExternalIdentityResolver resolver = new ClaimsExternalIdentityResolver(entraProperties());
        Jwt jwt = token().claim("oid", "oid-1").build();

        assertThrows(InvalidBearerTokenException.class, () -> resolver.resolve(jwt));
    }

    @Test
    void userInfoResolver_shouldFetchEmailFromUserInfoEndpoint() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(USERINFO_URI))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer access-token"))
                .andRespond(withSuccess("{\"sub\":\"sub-1\",\"email\":\"user@example.com\"}", MediaType.APPLICATION_JSON));
        UserInfoExternalIdentityResolver resolver = new UserInfoExternalIdentityResolver(cognitoProperties(), builder.build());

        ExternalIdentity identity = resolver.resolve(token().subject("sub-1").build());

        assertEquals(new ExternalIdentity("cognito", "sub-1", "user@example.com"), identity);
        server.verify();
    }

    @Test
    void userInfoResolver_shouldRejectUserInfoOfAnotherSubject() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(USERINFO_URI))
                .andRespond(withSuccess("{\"sub\":\"someone-else\",\"email\":\"user@example.com\"}", MediaType.APPLICATION_JSON));
        UserInfoExternalIdentityResolver resolver = new UserInfoExternalIdentityResolver(cognitoProperties(), builder.build());

        Jwt jwt = token().subject("sub-1").build();

        assertThrows(InvalidBearerTokenException.class, () -> resolver.resolve(jwt));
    }

    @Test
    void userInfoResolver_shouldRejectUserInfoWithoutEmail() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(USERINFO_URI))
                .andRespond(withSuccess("{\"sub\":\"sub-1\"}", MediaType.APPLICATION_JSON));
        UserInfoExternalIdentityResolver resolver = new UserInfoExternalIdentityResolver(cognitoProperties(), builder.build());

        Jwt jwt = token().subject("sub-1").build();

        assertThrows(InvalidBearerTokenException.class, () -> resolver.resolve(jwt));
    }

    @Test
    void userInfoResolver_shouldRejectWhenEndpointFails() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(USERINFO_URI)).andRespond(withServerError());
        UserInfoExternalIdentityResolver resolver = new UserInfoExternalIdentityResolver(cognitoProperties(), builder.build());

        Jwt jwt = token().subject("sub-1").build();

        assertThrows(InvalidBearerTokenException.class, () -> resolver.resolve(jwt));
    }

    @Test
    void userInfoResolver_shouldRequireUserInfoUri() {
        AuthProviderProperties properties = cognitoProperties();
        properties.setUserinfoUri("");

        RestClient restClient = RestClient.create();
        assertThrows(IllegalStateException.class, () -> new UserInfoExternalIdentityResolver(properties, restClient));
    }
}
