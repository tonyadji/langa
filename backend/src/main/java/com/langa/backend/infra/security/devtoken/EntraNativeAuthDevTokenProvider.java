package com.langa.backend.infra.security.devtoken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Microsoft Entra External ID implementation, based on the native authentication API with email one-time codes:
 * <ul>
 *     <li>sign-in: /oauth2/v2.0/initiate, /oauth2/v2.0/challenge, then /oauth2/v2.0/token</li>
 *     <li>sign-up: /signup/v1.0/start, /signup/v1.0/challenge, /signup/v1.0/continue (code, then attributes if
 *     required by the user flow), then /oauth2/v2.0/token with the continuation token</li>
 * </ul>
 * The client must be a public client with native authentication enabled.
 */
@Slf4j
public class EntraNativeAuthDevTokenProvider implements DevTokenProvider {

    /** Challenge types supported by this client: email one-time passcode, or fall back to the browser. */
    static final String CHALLENGE_TYPES = "oob redirect";
    static final String DISPLAY_NAME_ATTRIBUTE = "displayName";

    private static final String SIGN_IN = "/oauth2/v2.0";
    private static final String SIGN_UP = "/signup/v1.0";
    private static final ParameterizedTypeReference<Map<String, Object>> JSON = new ParameterizedTypeReference<>() {};

    private final DevTokenProperties properties;
    private final RestClient restClient;
    private final String tenantUri;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * State of a sign-in/sign-up between /start and /complete, returned to the caller as an opaque token.
     *
     * @param flow        {@code signin} or {@code signup}
     * @param token       Entra continuation token
     * @param username    email of the user
     * @param displayName display name used if the sign-up requires it
     */
    record Continuation(String flow, String token, String username, String displayName) {
        static final String SIGN_IN_FLOW = "signin";
        static final String SIGN_UP_FLOW = "signup";
    }

    public EntraNativeAuthDevTokenProvider(DevTokenProperties properties, RestClient restClient) {
        if (isBlank(properties.getClientId()) || isBlank(properties.getNativeAuthUri()) || isBlank(properties.getScope())) {
            throw new IllegalStateException(
                    "application.security.dev-token client-id, native-auth-uri and scope are required when enabled");
        }
        this.properties = properties;
        this.restClient = restClient;
        // Accept both the tenant URI and the former sign-in (.../oauth2/v2.0) URI
        this.tenantUri = properties.getNativeAuthUri().replaceAll("/+$", "").replaceAll("/oauth2/v2\\.0$", "");
    }

    @Override
    public DevTokenChallenge start(String username, boolean signUp, String displayName) {
        final Map<String, Object> initiate = post(SIGN_IN + "/initiate", form(
                "challenge_type", CHALLENGE_TYPES,
                "username", username));
        if (signUp && "user_not_found".equals(initiate.get("error"))) {
            return startSignUp(username, displayName);
        }
        failOnError(initiate);

        // Sends the one-time code to the user's email
        final Map<String, Object> challenge = post(SIGN_IN + "/challenge", form(
                "challenge_type", CHALLENGE_TYPES,
                "continuation_token", continuationToken(initiate)));
        return toChallenge(challenge, new Continuation(Continuation.SIGN_IN_FLOW, null, username, null));
    }

    private DevTokenChallenge startSignUp(String username, String displayName) {
        log.info("Signing up a new user through the development token endpoint");
        final Map<String, Object> start = post(SIGN_UP + "/start", form(
                "challenge_type", CHALLENGE_TYPES,
                "username", username));
        if ("user_already_exists".equals(start.get("error"))) {
            // Created in the meantime: normal sign-in
            return start(username, false, displayName);
        }
        failOnError(start);

        // Sends the one-time code to the user's email
        final Map<String, Object> challenge = post(SIGN_UP + "/challenge", form(
                "challenge_type", CHALLENGE_TYPES,
                "continuation_token", continuationToken(start)));
        return toChallenge(challenge, new Continuation(Continuation.SIGN_UP_FLOW, null, username,
                isBlank(displayName) ? defaultDisplayName(username) : displayName.trim()));
    }

    private DevTokenChallenge toChallenge(Map<String, Object> challenge, Continuation continuation) {
        failOnError(challenge);
        if (!"oob".equals(challenge.get("challenge_type"))) {
            throw unsupported(challenge);
        }
        final Continuation withToken = new Continuation(continuation.flow(), continuationToken(challenge),
                continuation.username(), continuation.displayName());
        return new DevTokenChallenge(
                encode(withToken),
                stringValue(challenge.get("challenge_target_label")),
                challenge.get("code_length") instanceof Number length ? length.intValue() : null,
                Continuation.SIGN_UP_FLOW.equals(continuation.flow()));
    }

    @Override
    public DevToken complete(String continuationToken, String code) {
        final Continuation continuation = decode(continuationToken);
        if (Continuation.SIGN_UP_FLOW.equals(continuation.flow())) {
            return completeSignUp(continuation, code);
        }
        return token(form(
                "grant_type", "oob",
                "continuation_token", continuation.token(),
                "oob", code,
                "scope", properties.getScope()));
    }

    private DevToken completeSignUp(Continuation continuation, String code) {
        Map<String, Object> response = post(SIGN_UP + "/continue", form(
                "grant_type", "oob",
                "continuation_token", continuation.token(),
                "oob", code));

        if ("attributes_required".equals(response.get("error"))) {
            response = post(SIGN_UP + "/continue", form(
                    "grant_type", "attributes",
                    "continuation_token", continuationToken(response),
                    "attributes", requiredAttributes(response, continuation)));
        }
        failOnError(response);

        // Sign-up completed: sign the new user in with the continuation token
        return token(form(
                "grant_type", "continuation_token",
                "continuation_token", continuationToken(response),
                "username", continuation.username(),
                "scope", properties.getScope()));
    }

    /** Only the display name can be provided automatically; other required attributes make the sign-up fail. */
    private String requiredAttributes(Map<String, Object> response, Continuation continuation) {
        final List<String> required = response.get("required_attributes") instanceof List<?> attributes
                ? attributes.stream()
                    .filter(Map.class::isInstance)
                    .map(attribute -> stringValue(((Map<?, ?>) attribute).get("name")))
                    .toList()
                : List.of();
        final List<String> unsupported = required.stream()
                .filter(name -> !DISPLAY_NAME_ATTRIBUTE.equals(name))
                .toList();
        if (!unsupported.isEmpty()) {
            throw new GenericException("The user flow requires attributes that cannot be provided: "
                    + String.join(", ", unsupported), null, Errors.DEV_TOKEN_ATTRIBUTES_REQUIRED);
        }
        return toJson(Map.of(DISPLAY_NAME_ATTRIBUTE, continuation.displayName()));
    }

    private DevToken token(MultiValueMap<String, String> body) {
        final Map<String, Object> token = post(SIGN_IN + "/token", body);
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
                    .uri(tenantUri + path)
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
            case "user_not_found" -> new GenericException(
                    "No account with this email (pass \"signUp\": true to create it)", null, Errors.USER_NOT_FOUND);
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

    private String encode(Continuation continuation) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(toJson(continuation).getBytes(StandardCharsets.UTF_8));
    }

    private Continuation decode(String continuationToken) {
        try {
            final Continuation continuation = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(continuationToken), Continuation.class);
            if (isBlank(continuation.token()) || isBlank(continuation.flow())) {
                throw new IllegalArgumentException("incomplete continuation");
            }
            return continuation;
        } catch (Exception e) {
            throw new GenericException("Invalid continuation token", null, Errors.DEV_TOKEN_INVALID_CONTINUATION);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String defaultDisplayName(String username) {
        final int at = username.indexOf('@');
        return at > 0 ? username.substring(0, at) : username;
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
