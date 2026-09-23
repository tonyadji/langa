package com.langa.backend.infra.security.devtoken;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Microsoft Entra External ID implementation, based on the native authentication API
 * (email one-time passcode sign-in): /initiate, /challenge, then /token.
 * The client must be a public client with native authentication enabled.
 */
@Slf4j
public class EntraNativeAuthDevTokenProvider implements DevTokenProvider {

    /** Challenge types supported by this client: email one-time passcode, or fall back to the browser. */
    static final String CHALLENGE_TYPES = "oob redirect";

    private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {};

    private final DevTokenProperties properties;
    private final RestClient restClient;

    public EntraNativeAuthDevTokenProvider(DevTokenProperties properties, RestClient restClient) {
        if (isBlank(properties.getClientId()) || isBlank(properties.getNativeAuthUri()) || isBlank(properties.getScope())) {
            throw new IllegalStateException(
                    "application.security.dev-token client-id, native-auth-uri and scope are required when enabled");
        }
        this.properties = properties;
        this.restClient = restClient;
    }

    @Override
    public DevTokenChallenge startSignIn(String username) {
        final Map<String, Object> initiate = post("/initiate", form(
                "challenge_type", CHALLENGE_TYPES,
                "username", username));
        failOnError(initiate);
        final String initiateToken = continuationToken(initiate);

        // Sends the one-time code to the user's email
        final Map<String, Object> challenge = post("/challenge", form(
                "challenge_type", CHALLENGE_TYPES,
                "continuation_token", initiateToken));
        failOnError(challenge);
        if (!"oob".equals(challenge.get("challenge_type"))) {
            throw unsupported(challenge);
        }
        return new DevTokenChallenge(
                continuationToken(challenge),
                stringValue(challenge.get("challenge_target_label")),
                challenge.get("code_length") instanceof Number length ? length.intValue() : null);
    }

    @Override
    public DevToken completeSignIn(String continuationToken, String code) {
        final Map<String, Object> token = post("/token", form(
                "grant_type", "oob",
                "continuation_token", continuationToken,
                "oob", code,
                "scope", properties.getScope()));
        failOnError(token);

        final String accessToken = stringValue(token.get("access_token"));
        if (isBlank(accessToken)) {
            throw providerError("no access token in the response");
        }
        return new DevToken(
                accessToken,
                token.get("token_type") instanceof String type ? type : "Bearer",
                token.get("expires_in") instanceof Number expiresIn ? expiresIn.longValue() : null);
    }

    private Map<String, Object> post(String path, MultiValueMap<String, String> body) {
        body.add("client_id", properties.getClientId());
        try {
            // Error responses (4xx) carry the reason in their JSON body: read the body whatever the status
            final Map<String, Object> response = restClient.post()
                    .uri(properties.getNativeAuthUri() + path)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .exchange((request, clientResponse) -> clientResponse.bodyTo(JSON));
            if (response == null) {
                throw providerError("empty response from " + path);
            }
            return response;
        } catch (RestClientException e) {
            log.error("Native authentication call {} failed", path, e);
            throw providerError("call to " + path + " failed");
        }
    }

    private static void failOnError(Map<String, Object> response) {
        if ("redirect".equals(response.get("challenge_type"))) {
            throw unsupported(response);
        }
        final String error = stringValue(response.get("error"));
        if (error == null) {
            return;
        }
        final String suberror = stringValue(response.get("suberror"));
        log.warn("Native authentication error: {} {} {}", error, suberror, description(response));
        throw switch (error) {
            case "user_not_found" -> new GenericException("No account with this email", null, Errors.USER_NOT_FOUND);
            case "expired_token" -> new GenericException("Sign-in session expired", null, Errors.DEV_TOKEN_SESSION_EXPIRED);
            case "invalid_grant" -> "invalid_oob_value".equals(suberror)
                    ? new GenericException("Invalid verification code", null, Errors.DEV_TOKEN_INVALID_CODE)
                    : providerError(error + " " + description(response));
            default -> providerError(error + " " + description(response));
        };
    }

    private static GenericException unsupported(Map<String, Object> response) {
        log.warn("Native authentication requires the browser (challenge_type={})", response.get("challenge_type"));
        return new GenericException(
                "This account or client cannot sign in with an email code: use the frontend",
                null, Errors.DEV_TOKEN_SIGN_IN_UNSUPPORTED);
    }

    private static GenericException providerError(String detail) {
        return new GenericException("Identity provider error: " + detail, null, Errors.IDENTITY_PROVIDER_ERROR);
    }

    private static String continuationToken(Map<String, Object> response) {
        final String token = stringValue(response.get("continuation_token"));
        if (isBlank(token)) {
            throw providerError("no continuation token in the response");
        }
        return token;
    }

    /** First line of the provider description, without its trace/correlation ids. */
    private static String description(Map<String, Object> response) {
        final String description = stringValue(response.get("error_description"));
        return description == null ? "" : description.split("Trace ID")[0].trim();
    }

    private static MultiValueMap<String, String> form(String... keyValues) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            form.add(keyValues[i], keyValues[i + 1]);
        }
        return form;
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
