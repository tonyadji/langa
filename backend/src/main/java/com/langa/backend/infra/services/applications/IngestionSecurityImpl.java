package com.langa.backend.infra.services.applications;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.infra.security.utils.HMACUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class IngestionSecurityImpl implements IngestionSecurity {
    @Value("${application.agent-version}")
    private String agentVersion;

    @Override
    public boolean isAuthorized(IngestionCredentials credentials, Application app) {
        if(!Objects.equals(agentVersion, credentials.userAgent())) {
            throw new IngestionSecurityException("Invalid agent version", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }
        if(!Objects.equals(credentials.appKey(), app.getKey()) || !Objects.equals(credentials.accountKey(), app.getAccountKey())) {
            throw new IngestionSecurityException("Illegal app", null, Errors.ILLEGAL_INGESTION_REQUEST);
        }
        //validate nonce usage
        //validate timestamp

        //TODO: refactor this class
        return evaluateSignature(credentials.appKey(), credentials.accountKey(), credentials.timestamp(), credentials.signature(), credentials.credentialType(), app);
    }

    private boolean evaluateSignature(String appKey, String accountKey, String timestamp, String signature, CredentialType credentialType, Application app) {
        final String[] signatureParts = signature.split(":");
        final String nonce = signatureParts[0];
        final String actualSignature = signatureParts[1];
        final String expectedSignature = buildSignature(nonce, appKey, accountKey, timestamp, credentialType, app.getSecret());
        log.info("Expected signature: {}", expectedSignature);
        log.info("Actual signature: {}", actualSignature);
        return Objects.equals(expectedSignature, actualSignature);
    }

    private String buildSignature(String nonce, String appKey, String accountKey, String timestamp, CredentialType credentialType, String appSecret) {
        String concatMessage = appKey
                .concat(accountKey)
                .concat(agentVersion)
                .concat(timestamp)
                .concat(nonce)
                .concat(credentialType.name());
        return nonce + ":" + HMACUtils.hash(concatMessage, appSecret);
    }
}
