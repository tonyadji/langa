package com.langa.backend.infra.security.identity;

import com.langa.backend.infra.security.config.AuthProviderProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Reads the subject from the access token and the email from the OIDC UserInfo endpoint,
 * for providers whose access tokens carry no email (e.g. Amazon Cognito).
 */
public class UserInfoExternalIdentityResolver extends ClaimsExternalIdentityResolver {

    private static final ParameterizedTypeReference<Map<String, Object>> USER_INFO_TYPE = new ParameterizedTypeReference<>() {};

    private final AuthProviderProperties properties;
    private final RestClient restClient;

    public UserInfoExternalIdentityResolver(AuthProviderProperties properties, RestClient restClient) {
        super(properties);
        if (properties.getUserinfoUri() == null || properties.getUserinfoUri().isBlank()) {
            throw new IllegalStateException("application.security.auth.userinfo-uri is required when email-source is userinfo");
        }
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    protected String email(Jwt jwt) {
        final Map<String, Object> userInfo;
        try {
            userInfo = restClient.get()
                    .uri(properties.getUserinfoUri())
                    .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                    .retrieve()
                    .body(USER_INFO_TYPE);
        } catch (RestClientException e) {
            throw new InvalidBearerTokenException("Unable to fetch the user info", e);
        }

        final Object subject = userInfo == null ? null : userInfo.get("sub");
        final Object email = userInfo == null ? null : userInfo.get(properties.getEmailClaim());
        // OIDC: the UserInfo response must be about the token subject
        if (subject != null && !subject.toString().equals(jwt.getSubject())) {
            throw new InvalidBearerTokenException("User info subject does not match the access token");
        }
        if (email == null || email.toString().isBlank()) {
            throw new InvalidBearerTokenException("User info has no '" + properties.getEmailClaim() + "' attribute");
        }
        return email.toString();
    }
}
