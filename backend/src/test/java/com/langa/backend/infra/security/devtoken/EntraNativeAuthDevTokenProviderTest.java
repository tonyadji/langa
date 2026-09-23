package com.langa.backend.infra.security.devtoken;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class EntraNativeAuthDevTokenProviderTest {

    private static final String TENANT = "https://langa-test.ciamlogin.com/tenant-id";
    private static final String SIGN_IN = TENANT + "/oauth2/v2.0";
    private static final String SIGN_UP = TENANT + "/signup/v1.0";
    private static final String CLIENT_ID = "test-client-id";
    private static final String SCOPE = "api://api-client-id/access_as_user";
    private static final String TOKEN_RESPONSE =
            "{'token_type':'Bearer','expires_in':3599,'access_token':'eyJ.access','refresh_token':'r','id_token':'i'}";

    private MockRestServiceServer server;
    private EntraNativeAuthDevTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = newProvider(TENANT);
    }

    private EntraNativeAuthDevTokenProvider newProvider(String nativeAuthUri) {
        DevTokenProperties properties = new DevTokenProperties();
        properties.setEnabled(true);
        properties.setClientId(CLIENT_ID);
        properties.setNativeAuthUri(nativeAuthUri);
        properties.setScope(SCOPE);
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        return new EntraNativeAuthDevTokenProvider(properties, builder.build());
    }

    /** Opaque token as returned by start(), built directly since the mock server cannot be reset mid-test. */
    private static String continuation(String flow, String token, String username, String displayName) {
        String json = "{\"flow\":\"" + flow + "\",\"token\":\"" + token + "\",\"username\":\"" + username + "\","
                + "\"displayName\":" + (displayName == null ? "null" : "\"" + displayName + "\"") + "}";
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String json(String body) {
        return body.replace('\'', '"');
    }

    private ResponseActions expect(String url, Map<String, String> form) {
        return server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formDataContains(form));
    }

    private void respond(ResponseActions actions, String body) {
        actions.andRespond(withSuccess(json(body), MediaType.APPLICATION_JSON));
    }

    private void respondError(ResponseActions actions, String body) {
        actions.andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(json(body)));
    }

    private void expectSignInStart() {
        respond(expect(SIGN_IN + "/initiate", Map.of(
                        "client_id", CLIENT_ID, "username", "user@example.com", "challenge_type", "oob redirect")),
                "{'continuation_token':'ct-1'}");
        respond(expect(SIGN_IN + "/challenge", Map.of(
                        "client_id", CLIENT_ID, "continuation_token", "ct-1", "challenge_type", "oob redirect")),
                "{'challenge_type':'oob','challenge_target_label':'u***@example.com','code_length':8,'continuation_token':'ct-2'}");
    }

    private void expectSignUpStart(String username) {
        respondError(expect(SIGN_IN + "/initiate", Map.of("username", username)),
                "{'error':'user_not_found','error_description':'AADSTS50034: The user account does not exist.'}");
        respond(expect(SIGN_UP + "/start", Map.of(
                        "client_id", CLIENT_ID, "username", username, "challenge_type", "oob redirect")),
                "{'continuation_token':'su-1'}");
        respond(expect(SIGN_UP + "/challenge", Map.of(
                        "client_id", CLIENT_ID, "continuation_token", "su-1", "challenge_type", "oob redirect")),
                "{'challenge_type':'oob','challenge_target_label':'n***@example.com','code_length':8,'continuation_token':'su-2'}");
    }

    // ------------------------------------------------------------------ sign-in

    @Test
    void start_shouldInitiateThenSendTheEmailCode() {
        expectSignInStart();

        DevTokenProvider.DevTokenChallenge challenge = provider.start("user@example.com", false, null);

        assertEquals("u***@example.com", challenge.sentTo());
        assertEquals(8, challenge.codeLength());
        assertFalse(challenge.signUp());
        assertEquals(continuation("signin", "ct-2", "user@example.com", null), challenge.continuationToken(),
                "the provider token is wrapped in an opaque token");
        server.verify();
    }

    @Test
    void start_shouldSignInAnExistingUserEvenWhenSignUpIsRequested() {
        expectSignInStart();

        DevTokenProvider.DevTokenChallenge challenge = provider.start("user@example.com", true, null);

        assertFalse(challenge.signUp());
        server.verify();
    }

    @Test
    void complete_shouldExchangeTheCodeForAnAccessToken() {
        String continuation = continuation("signin", "ct-2", "user@example.com", null);
        respond(expect(SIGN_IN + "/token", Map.of(
                        "client_id", CLIENT_ID, "grant_type", "oob", "continuation_token", "ct-2",
                        "oob", "12345678", "scope", SCOPE)),
                TOKEN_RESPONSE);

        DevTokenProvider.DevToken token = provider.complete(continuation, "12345678");

        assertEquals("eyJ.access", token.accessToken());
        assertEquals("Bearer", token.tokenType());
        assertEquals(3599L, token.expiresIn());
        server.verify();
    }

    @Test
    void start_shouldReportUnknownUserWhenSignUpIsNotRequested() {
        respondError(server.expect(requestTo(SIGN_IN + "/initiate")),
                "{'error':'user_not_found','error_description':'AADSTS50034: The user account does not exist. Trace ID: x'}");

        GenericException error = assertThrows(GenericException.class, () -> provider.start("nobody@example.com", false, null));

        assertEquals(Errors.USER_NOT_FOUND, error.getError());
        assertTrue(error.getMessage().contains("signUp"));
        server.verify();
    }

    @Test
    void start_shouldReportAccountsRequiringTheBrowser() {
        respond(server.expect(requestTo(SIGN_IN + "/initiate")), "{'challenge_type':'redirect'}");

        GenericException error = assertThrows(GenericException.class, () -> provider.start("user@example.com", false, null));

        assertEquals(Errors.DEV_TOKEN_SIGN_IN_UNSUPPORTED, error.getError());
    }

    @Test
    void start_shouldReportPasswordChallengeAsUnsupported() {
        respond(server.expect(requestTo(SIGN_IN + "/initiate")), "{'continuation_token':'ct-1'}");
        respond(server.expect(requestTo(SIGN_IN + "/challenge")), "{'challenge_type':'password','continuation_token':'ct-2'}");

        GenericException error = assertThrows(GenericException.class, () -> provider.start("user@example.com", false, null));

        assertEquals(Errors.DEV_TOKEN_SIGN_IN_UNSUPPORTED, error.getError());
    }

    @Test
    void complete_shouldReportAWrongCode() {
        String continuation = continuation("signin", "ct-2", "user@example.com", null);
        respondError(server.expect(requestTo(SIGN_IN + "/token")),
                "{'error':'invalid_grant','suberror':'invalid_oob_value','error_description':'AADSTS50181: bad code'}");

        GenericException error = assertThrows(GenericException.class, () -> provider.complete(continuation, "00000000"));

        assertEquals(Errors.DEV_TOKEN_INVALID_CODE, error.getError());
    }

    @Test
    void complete_shouldReportAnExpiredSession() {
        String continuation = continuation("signin", "ct-2", "user@example.com", null);
        respondError(server.expect(requestTo(SIGN_IN + "/token")),
                "{'error':'expired_token','error_description':'AADSTS901007: expired'}");

        GenericException error = assertThrows(GenericException.class, () -> provider.complete(continuation, "12345678"));

        assertEquals(Errors.DEV_TOKEN_SESSION_EXPIRED, error.getError());
    }

    @Test
    void complete_shouldReportOtherProviderErrorsWithoutTraceIds() {
        String continuation = continuation("signin", "ct-2", "user@example.com", null);
        respondError(server.expect(requestTo(SIGN_IN + "/token")),
                "{'error':'invalid_scope','error_description':'AADSTS70011: invalid scope. Trace ID: abc'}");

        GenericException error = assertThrows(GenericException.class, () -> provider.complete(continuation, "12345678"));

        assertEquals(Errors.IDENTITY_PROVIDER_ERROR, error.getError());
        assertTrue(error.getMessage().contains("invalid_scope"));
        assertFalse(error.getMessage().contains("Trace ID"));
    }

    @Test
    void complete_shouldRejectAForgedContinuationToken() {
        GenericException error = assertThrows(GenericException.class, () -> provider.complete("not-a-token", "12345678"));

        assertEquals(Errors.DEV_TOKEN_INVALID_CONTINUATION, error.getError());
    }

    // ------------------------------------------------------------------ sign-up

    @Test
    void start_shouldSignUpAnUnknownUserWhenRequested() {
        expectSignUpStart("new@example.com");

        DevTokenProvider.DevTokenChallenge challenge = provider.start("new@example.com", true, null);

        assertTrue(challenge.signUp());
        assertEquals("n***@example.com", challenge.sentTo());
        assertEquals(continuation("signup", "su-2", "new@example.com", "new"), challenge.continuationToken());
        server.verify();
    }

    @Test
    void start_shouldSignInWhenTheUserWasCreatedInTheMeantime() {
        respondError(server.expect(requestTo(SIGN_IN + "/initiate")), "{'error':'user_not_found'}");
        respondError(server.expect(requestTo(SIGN_UP + "/start")), "{'error':'user_already_exists'}");
        expectSignInStart();

        DevTokenProvider.DevTokenChallenge challenge = provider.start("user@example.com", true, null);

        assertFalse(challenge.signUp());
        server.verify();
    }

    @Test
    void complete_shouldFinishTheSignUpThenReturnAnAccessToken() {
        String continuation = continuation("signup", "su-2", "new@example.com", "new");
        respond(expect(SIGN_UP + "/continue", Map.of(
                        "client_id", CLIENT_ID, "grant_type", "oob", "continuation_token", "su-2", "oob", "12345678")),
                "{'continuation_token':'su-3'}");
        respond(expect(SIGN_IN + "/token", Map.of(
                        "client_id", CLIENT_ID, "grant_type", "continuation_token", "continuation_token", "su-3",
                        "username", "new@example.com", "scope", SCOPE)),
                TOKEN_RESPONSE);

        DevTokenProvider.DevToken token = provider.complete(continuation, "12345678");

        assertEquals("eyJ.access", token.accessToken());
        server.verify();
    }

    @Test
    void complete_shouldProvideTheDisplayNameWhenTheUserFlowRequiresIt() {
        String continuation = continuation("signup", "su-2", "new@example.com", "new");
        respondError(expect(SIGN_UP + "/continue", Map.of("grant_type", "oob")),
                "{'error':'attributes_required','continuation_token':'su-3',"
                        + "'required_attributes':[{'name':'displayName','type':'string','required':true}]}");
        respond(expect(SIGN_UP + "/continue", Map.of(
                        "client_id", CLIENT_ID, "grant_type", "attributes", "continuation_token", "su-3",
                        "attributes", "{\"displayName\":\"new\"}")),
                "{'continuation_token':'su-4'}");
        respond(expect(SIGN_IN + "/token", Map.of("grant_type", "continuation_token", "continuation_token", "su-4")),
                TOKEN_RESPONSE);

        DevTokenProvider.DevToken token = provider.complete(continuation, "12345678");

        assertEquals("eyJ.access", token.accessToken());
        server.verify();
    }

    @Test
    void complete_shouldUseTheRequestedDisplayName() {
        String continuation = continuation("signup", "su-2", "new@example.com", "New User");
        respondError(server.expect(requestTo(SIGN_UP + "/continue")),
                "{'error':'attributes_required','continuation_token':'su-3','required_attributes':[{'name':'displayName'}]}");
        respond(expect(SIGN_UP + "/continue", Map.of("attributes", "{\"displayName\":\"New User\"}")),
                "{'continuation_token':'su-4'}");
        respond(server.expect(requestTo(SIGN_IN + "/token")), TOKEN_RESPONSE);

        provider.complete(continuation, "12345678");

        server.verify();
    }

    @Test
    void complete_shouldReportRequiredAttributesThatCannotBeProvided() {
        String continuation = continuation("signup", "su-2", "new@example.com", "new");
        respondError(server.expect(requestTo(SIGN_UP + "/continue")),
                "{'error':'attributes_required','continuation_token':'su-3',"
                        + "'required_attributes':[{'name':'displayName'},{'name':'city'}]}");

        GenericException error = assertThrows(GenericException.class, () -> provider.complete(continuation, "12345678"));

        assertEquals(Errors.DEV_TOKEN_ATTRIBUTES_REQUIRED, error.getError());
        assertTrue(error.getMessage().contains("city"));
    }

    @Test
    void complete_shouldReportAWrongSignUpCode() {
        String continuation = continuation("signup", "su-2", "new@example.com", "new");
        respondError(server.expect(requestTo(SIGN_UP + "/continue")),
                "{'error':'invalid_grant','suberror':'invalid_oob_value'}");

        GenericException error = assertThrows(GenericException.class, () -> provider.complete(continuation, "00000000"));

        assertEquals(Errors.DEV_TOKEN_INVALID_CODE, error.getError());
    }

    // ------------------------------------------------------------------ configuration

    @Test
    void shouldAcceptTheFormerSignInUri() {
        provider = newProvider(SIGN_IN + "/");
        expectSignInStart();

        provider.start("user@example.com", false, null);

        server.verify();
    }

    @Test
    void shouldReportUnreachableProvider() {
        server.expect(requestTo(SIGN_IN + "/initiate")).andRespond(withServerError());

        GenericException error = assertThrows(GenericException.class, () -> provider.start("user@example.com", false, null));

        assertEquals(Errors.IDENTITY_PROVIDER_ERROR, error.getError());
    }

    @Test
    void shouldRequireConfiguration() {
        DevTokenProperties properties = new DevTokenProperties();
        properties.setEnabled(true);
        RestClient restClient = RestClient.create();

        assertThrows(IllegalStateException.class, () -> new EntraNativeAuthDevTokenProvider(properties, restClient));
    }
}
