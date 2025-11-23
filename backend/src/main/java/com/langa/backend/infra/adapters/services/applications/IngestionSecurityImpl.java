package com.langa.backend.infra.adapters.services.applications;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.repositories.ApplicationNonceRepository;
import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.infra.security.utils.HMACUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.langa.backend.infra.security.utils.HMACUtils.clean;

@Service
@Slf4j
public class IngestionSecurityImpl implements IngestionSecurity {
    private static final long MAX_AGE_SECONDS = 300;
    private static final long MAX_FUTURE_SECONDS = 5;
    @Value("${application.agent-version}")
    private String agentVersion;

    private final ApplicationNonceRepository applicationNonceRepository;

    public IngestionSecurityImpl(ApplicationNonceRepository applicationNonceRepository) {
        this.applicationNonceRepository = applicationNonceRepository;
    }

    @Override
    public boolean isAuthorized(IngestionCredentials credentials, Application app) {
        if(!Objects.equals(agentVersion, credentials.userAgent())) {
            log.error("Expected {} - received {}", agentVersion, credentials.userAgent());
            throw new IngestionSecurityException("Invalid agent version", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }
        if(!Objects.equals(credentials.appKey(), app.getKey()) || !Objects.equals(credentials.accountKey(), app.getAccountKey())) {
            throw new IngestionSecurityException("Illegal app", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }
        return evaluateSignature(credentials.appKey(), credentials.accountKey(), credentials.userAgent(), credentials.timestamp(), credentials.signature(), credentials.credentialType(), app);
    }

    private boolean evaluateSignature(String appKey, String accountKey, String agentVersion, String timestamp, String signature, CredentialType credentialType, Application app) {
        final String[] signatureParts = signature.split(":");
        final String nonce = signatureParts[0];

        if (signatureParts.length != 2) {
            throw new IngestionSecurityException("Invalid signature format", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }

        if (applicationNonceRepository.existsByAppKeyAndNonce(appKey, nonce)) {
            throw new IngestionSecurityException("Existing nonce detected", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }

        checkTimestampValidity(timestamp);

        final String expectedSignature = buildSignature(nonce, appKey, accountKey, agentVersion, timestamp, credentialType, app.getSecret());
        if (!Objects.equals(expectedSignature, signature)) {
            log.error("expected {} - received {}", expectedSignature, signature);
            throw new IngestionSecurityException("Signature mismatch", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }
        applicationNonceRepository.save(appKey, nonce, LocalDateTime.now());
        return true;
    }

    private String buildSignature(String nonce, String appKey, String accountKey, String agentVersion, String timestamp, CredentialType credentialType, String appSecret) {
        String concatMessage = clean(appKey)
                .concat(clean(accountKey))
                .concat(clean(agentVersion))
                .concat(clean(timestamp))
                .concat(clean(nonce))
                .concat(credentialType.name());
        return nonce + ":" + HMACUtils.hash(concatMessage, appSecret.trim());
    }

    private void checkTimestampValidity(String timestampStr) {
        try {
            long receivedMillis = Long.parseLong(timestampStr);
            long currentMillis = System.currentTimeMillis();

            if (receivedMillis > currentMillis + (MAX_FUTURE_SECONDS * 1000L)) {
                throw new IngestionSecurityException("Timestamp too far in the future", null, Errors.ILLEGAL_INGESTION_REQUEST);
            }

            if (receivedMillis < currentMillis - (MAX_AGE_SECONDS * 1000L)) {
                throw new IngestionSecurityException("Timestamp expired", null, Errors.ILLEGAL_INGESTION_REQUEST);
            }
        } catch (NumberFormatException e) {
            throw new IngestionSecurityException("Invalid timestamp format", e, Errors.ILLEGAL_INGESTION_REQUEST);
        }
    }
}
