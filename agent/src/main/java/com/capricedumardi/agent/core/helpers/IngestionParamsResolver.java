package com.capricedumardi.agent.core.helpers;

import com.capricedumardi.agent.core.model.SenderType;

import java.util.Base64;

public record IngestionParamsResolver(String ingestionUrl, String secret) {

    private static final String INGESTION_ENDPOINT = "/api/ingestion";
    private static final String CREDENTIALS_DELIMITER = "/";
    private static final String KEY_DELIMITER = "-lga-";
    public String resolveHttpUrl() {
        return ingestionUrl.split(INGESTION_ENDPOINT)[0] + INGESTION_ENDPOINT;
    }

    public SenderType resolveSenderType() {
        return ingestionUrl.split(INGESTION_ENDPOINT+CREDENTIALS_DELIMITER)[1].startsWith("h") ? SenderType.HTTP : SenderType.KAFKA;
    }

    public String resolveBootStrapServer() {
        return ingestionUrl.split(INGESTION_ENDPOINT)[0];
    }

    public String resolveTopic() {
        return ingestionUrl.split(INGESTION_ENDPOINT+CREDENTIALS_DELIMITER)[1].split(CREDENTIALS_DELIMITER)[0];
    }

    public String resolveAppKey() {
        final String encodedCredentials = ingestionUrl.split(INGESTION_ENDPOINT+CREDENTIALS_DELIMITER)[1].split(CREDENTIALS_DELIMITER)[1];
        final String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
        return decodedCredentials.split(KEY_DELIMITER)[1];
    }

    public String resolveAccountKey() {
        final String encodedCredentials = ingestionUrl.split(INGESTION_ENDPOINT+CREDENTIALS_DELIMITER)[1].split(CREDENTIALS_DELIMITER)[1];
        final String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
        return decodedCredentials.split(KEY_DELIMITER)[0];
    }

    public String resolveSecret() {
        return secret;
    }
}
