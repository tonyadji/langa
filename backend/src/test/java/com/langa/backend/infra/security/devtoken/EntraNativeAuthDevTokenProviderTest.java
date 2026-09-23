package com.langa.backend.infra.security.devtoken;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class EntraNativeAuthDevTokenProviderTest {

    private static final String BASE = "https://langa-test.ciamlogin.com/tenant-id/oauth2/v2.0";
    private static final String CLIENT_ID = "test-client-id";
    private static final String SCOPE = "api://api-client-id/access_as_user";

    private MockRestServiceServer server;
    private EntraNativeAuthDevTokenProvider provider;

    @BeforeEach
    void setUp() {
        DevTokenProperties properties = new DevTokenProperties();
        properties.setEnabled(true);
        properties.setClientId(CLIENT_ID);
        properties.setNativeAuthUri(BASE);
        properties.setScope(SCOPE);
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        provider = new EntraNativeAuthDevTokenProvider(properties, builder.build());
    }

    private static String json(String body) {
        return body.replace('\'', '"');
    }

    @Test
    void startSignIn_shouldInitiateThenSendTheEmailCode() {
        server.expect(requestTo(BASE + "/initiate"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().formDataContains(java.util.Map.of(
                        "client_id", CLIENT_ID, "username", "user@example.com", "challenge_type", "oob redirect")))
                .andRespond(withSuccess(json("{'continuation_token':'ct-1'}"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/challenge"))
                .andExpect(content().formDataContains(java.util.Map.of(
                        "client_id", CLIENT_ID, "continuation_token", "ct-1", "challenge_type", "oob redirect")))
                .andRespond(withSuccess(json("{'challenge_type':'oob','challenge_target_label':'u***@example.com','code_length':8,'continuation_token':'ct-2'}"),
                        MediaType.APPLICATION_JSON));

        DevTokenProvider.DevTokenChallenge challenge = provider.startSignIn("user@example.com");

        assertEquals("ct-2", challenge.continuationToken());
        assertEquals("u***@example.com", challenge.sentTo());
        assertEquals(8, challenge.codeLength());
        server.verify();
    }

    @Test
    void startSignIn_shouldReportUnknownUser() {
        server.expect(requestTo(BASE + "/initiate"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body(json("{'error':'user_not_found','error_description':'AADSTS50034: The user account does not exist. Trace ID: x'}")));

        GenericException error = assertThrows(GenericException.class, () -> provider.startSignIn("nobody@example.com"));

        assertEquals(Errors.USER_NOT_FOUND, error.getError());
    }

    @Test
    void startSignIn_shouldReportAccountsRequiringTheBrowser() {
        server.expect(requestTo(BASE + "/initiate"))
                .andRespond(withSuccess(json("{'challenge_type':'redirect'}"), MediaType.APPLICATION_JSON));

        GenericException error = assertThrows(GenericException.class, () -> provider.startSignIn("user@example.com"));

        assertEquals(Errors.DEV_TOKEN_SIGN_IN_UNSUPPORTED, error.getError());
    }

    @Test
    void startSignIn_shouldReportPasswordChallengeAsUnsupported() {
        server.expect(requestTo(BASE + "/initiate"))
                .andRespond(withSuccess(json("{'continuation_token':'ct-1'}"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/challenge"))
                .andRespond(withSuccess(json("{'challenge_type':'password','continuation_token':'ct-2'}"), MediaType.APPLICATION_JSON));

        GenericException error = assertThrows(GenericException.class, () -> provider.startSignIn("user@example.com"));

        assertEquals(Errors.DEV_TOKEN_SIGN_IN_UNSUPPORTED, error.getError());
    }

    @Test
    void completeSignIn_shouldExchangeTheCodeForAnAccessToken() {
        server.expect(requestTo(BASE + "/token"))
                .andExpect(content().formDataContains(java.util.Map.of(
                        "client_id", CLIENT_ID, "grant_type", "oob", "continuation_token", "ct-2",
                        "oob", "12345678", "scope", SCOPE)))
                .andRespond(withSuccess(json("{'token_type':'Bearer','expires_in':3599,'access_token':'eyJ.access','refresh_token':'r','id_token':'i'}"),
                        MediaType.APPLICATION_JSON));

        DevTokenProvider.DevToken token = provider.completeSignIn("ct-2", "12345678");

        assertEquals("eyJ.access", token.accessToken());
        assertEquals("Bearer", token.tokenType());
        assertEquals(3599L, token.expiresIn());
        server.verify();
    }

    @Test
    void completeSignIn_shouldReportAWrongCode() {
        server.expect(requestTo(BASE + "/token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body(json("{'error':'invalid_grant','suberror':'invalid_oob_value','error_description':'AADSTS50181: bad code'}")));

        GenericException error = assertThrows(GenericException.class, () -> provider.completeSignIn("ct-2", "00000000"));

        assertEquals(Errors.DEV_TOKEN_INVALID_CODE, error.getError());
    }

    @Test
    void completeSignIn_shouldReportAnExpiredSession() {
        server.expect(requestTo(BASE + "/token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body(json("{'error':'expired_token','error_description':'AADSTS901007: expired'}")));

        GenericException error = assertThrows(GenericException.class, () -> provider.completeSignIn("ct-2", "12345678"));

        assertEquals(Errors.DEV_TOKEN_SESSION_EXPIRED, error.getError());
    }

    @Test
    void completeSignIn_shouldReportOtherProviderErrors() {
        server.expect(requestTo(BASE + "/token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body(json("{'error':'invalid_scope','error_description':'AADSTS70011: invalid scope. Trace ID: abc'}")));

        GenericException error = assertThrows(GenericException.class, () -> provider.completeSignIn("ct-2", "12345678"));

        assertEquals(Errors.IDENTITY_PROVIDER_ERROR, error.getError());
        assertTrue(error.getMessage().contains("invalid_scope"));
        assertFalse(error.getMessage().contains("Trace ID"));
    }

    @Test
    void shouldReportUnreachableProvider() {
        server.expect(requestTo(BASE + "/initiate")).andRespond(withServerError());

        GenericException error = assertThrows(GenericException.class, () -> provider.startSignIn("user@example.com"));

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
