package com.langa.backend.infra.adapters.services.applications;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.repositories.ApplicationNonceRepository;
import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.infra.security.utils.HMACUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionSecurityImplTest {

    private static final String AGENT_VERSION = "langa-agent-v1.0.0";

    @Mock
    private ApplicationNonceRepository applicationNonceRepository;

    private IngestionSecurityImpl ingestionSecurity;
    private Application application;

    @BeforeEach
    void setUp() {
        ingestionSecurity = new IngestionSecurityImpl(applicationNonceRepository);
        ReflectionTestUtils.setField(ingestionSecurity, "agentVersion", AGENT_VERSION);
        application = Application.createNew("My App", "ACC-KEY-1", "owner@example.com");
    }

    private IngestionCredentials validCredentials(String nonce, String timestamp) {
        String signature = signatureFor(nonce, timestamp, IngestionSecurity.CredentialType.HTTP);
        return new IngestionCredentials(AGENT_VERSION, application.getKey(), application.getAccountKey(),
                timestamp, signature, IngestionSecurity.CredentialType.HTTP);
    }

    private String signatureFor(String nonce, String timestamp, IngestionSecurity.CredentialType credentialType) {
        String concatMessage = application.getKey()
                .concat(application.getAccountKey())
                .concat(AGENT_VERSION)
                .concat(timestamp)
                .concat(nonce)
                .concat(credentialType.name());
        return nonce + ":" + HMACUtils.hash(concatMessage, application.getSecret().trim());
    }

    @Test
    void isAuthorized_shouldReturnTrue_andPersistNonce_forValidSignature() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        IngestionCredentials credentials = validCredentials("nonce-1", timestamp);
        when(applicationNonceRepository.existsByAppKeyAndNonce(application.getKey(), "nonce-1")).thenReturn(false);

        boolean result = ingestionSecurity.isAuthorized(credentials, application);

        assertTrue(result);
        verify(applicationNonceRepository).save(eq(application.getKey()), eq("nonce-1"), any(LocalDateTime.class));
    }

    @Test
    void isAuthorized_shouldThrow_whenAgentVersionMismatch() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        IngestionCredentials credentials = new IngestionCredentials("some-other-agent", application.getKey(),
                application.getAccountKey(), timestamp, "nonce-1:whatever", IngestionSecurity.CredentialType.HTTP);

        GenericException ex = assertThrows(GenericException.class,
                () -> ingestionSecurity.isAuthorized(credentials, application));

        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
        verifyNoInteractions(applicationNonceRepository);
    }

    @Test
    void isAuthorized_shouldThrow_whenAppKeyDoesNotMatch() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        IngestionCredentials credentials = new IngestionCredentials(AGENT_VERSION, "APP-WRONG-KEY",
                application.getAccountKey(), timestamp, "nonce-1:whatever", IngestionSecurity.CredentialType.HTTP);

        GenericException ex = assertThrows(GenericException.class,
                () -> ingestionSecurity.isAuthorized(credentials, application));

        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
    }

    @Test
    void isAuthorized_shouldThrow_whenAccountKeyDoesNotMatch() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        IngestionCredentials credentials = new IngestionCredentials(AGENT_VERSION, application.getKey(),
                "ACC-WRONG", timestamp, "nonce-1:whatever", IngestionSecurity.CredentialType.HTTP);

        GenericException ex = assertThrows(GenericException.class,
                () -> ingestionSecurity.isAuthorized(credentials, application));

        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
    }

    @Test
    void isAuthorized_shouldThrow_whenSignatureFormatIsInvalid() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        IngestionCredentials credentials = new IngestionCredentials(AGENT_VERSION, application.getKey(),
                application.getAccountKey(), timestamp, "no-colon-here", IngestionSecurity.CredentialType.HTTP);

        GenericException ex = assertThrows(GenericException.class,
                () -> ingestionSecurity.isAuthorized(credentials, application));

        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
    }

    @Test
    void isAuthorized_shouldThrow_whenNonceAlreadyUsed() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        IngestionCredentials credentials = validCredentials("reused-nonce", timestamp);
        when(applicationNonceRepository.existsByAppKeyAndNonce(application.getKey(), "reused-nonce")).thenReturn(true);

        GenericException ex = assertThrows(GenericException.class,
                () -> ingestionSecurity.isAuthorized(credentials, application));

        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
        verify(applicationNonceRepository, never()).save(anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    void isAuthorized_shouldThrow_whenTimestampTooOld() {
        String timestamp = String.valueOf(System.currentTimeMillis() - 400_000L);
        IngestionCredentials credentials = validCredentials("nonce-old", timestamp);
        when(applicationNonceRepository.existsByAppKeyAndNonce(application.getKey(), "nonce-old")).thenReturn(false);

        GenericException ex = assertThrows(GenericException.class,
                () -> ingestionSecurity.isAuthorized(credentials, application));

        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
    }

    @Test
    void isAuthorized_shouldThrow_whenTimestampTooFarInFuture() {
        String timestamp = String.valueOf(System.currentTimeMillis() + 60_000L);
        IngestionCredentials credentials = validCredentials("nonce-future", timestamp);
        when(applicationNonceRepository.existsByAppKeyAndNonce(application.getKey(), "nonce-future")).thenReturn(false);

        GenericException ex = assertThrows(GenericException.class,
                () -> ingestionSecurity.isAuthorized(credentials, application));

        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
    }

    @Test
    void isAuthorized_shouldThrow_whenTimestampIsNotNumeric() {
        IngestionCredentials credentials = validCredentials("nonce-x", "not-a-timestamp");
        when(applicationNonceRepository.existsByAppKeyAndNonce(application.getKey(), "nonce-x")).thenReturn(false);

        GenericException ex = assertThrows(GenericException.class,
                () -> ingestionSecurity.isAuthorized(credentials, application));

        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
    }

    @Test
    void isAuthorized_shouldThrow_whenSignatureDoesNotMatch() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        IngestionCredentials credentials = new IngestionCredentials(AGENT_VERSION, application.getKey(),
                application.getAccountKey(), timestamp, "nonce-1:tampered-signature", IngestionSecurity.CredentialType.HTTP);
        when(applicationNonceRepository.existsByAppKeyAndNonce(application.getKey(), "nonce-1")).thenReturn(false);

        GenericException ex = assertThrows(GenericException.class,
                () -> ingestionSecurity.isAuthorized(credentials, application));

        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
        verify(applicationNonceRepository, never()).save(anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    void isAuthorized_shouldThrow_whenSignatureBuiltWithWrongSecret() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String concatMessage = application.getKey()
                .concat(application.getAccountKey())
                .concat(AGENT_VERSION)
                .concat(timestamp)
                .concat("nonce-2")
                .concat(IngestionSecurity.CredentialType.HTTP.name());
        String signature = "nonce-2:" + HMACUtils.hash(concatMessage, "a-completely-different-secret");
        IngestionCredentials credentials = new IngestionCredentials(AGENT_VERSION, application.getKey(),
                application.getAccountKey(), timestamp, signature, IngestionSecurity.CredentialType.HTTP);
        when(applicationNonceRepository.existsByAppKeyAndNonce(application.getKey(), "nonce-2")).thenReturn(false);

        GenericException ex = assertThrows(GenericException.class,
                () -> ingestionSecurity.isAuthorized(credentials, application));

        assertEquals(Errors.ILLEGAL_INGESTION_REQUEST, ex.getError());
    }
}
