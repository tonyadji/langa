package com.capricedumardi.agent.core.helpers;

import com.capricedumardi.agent.core.model.SenderType;

import java.util.Base64;

public record IngestionParamsResolver(String ingestionUrl, String secret) {

    private static final String INGESTION_ENDPOINT = "/api/ingestion";
    private static final String CREDENTIALS_DELIMITER = "/";
    private static final String KEY_DELIMITER = "-lga-";

    public IngestionParamsResolver {
        if (ingestionUrl == null || ingestionUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("ingestionUrl cannot be null or empty");
        }
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("secret cannot be null or empty");
        }

        ingestionUrl = ingestionUrl.trim();
        secret = secret.trim();

        if (!ingestionUrl.contains(INGESTION_ENDPOINT)) {
            throw new IllegalArgumentException(
                    "Invalid ingestionUrl: must contain '" + INGESTION_ENDPOINT + "'. " +
                            "Got: " + ingestionUrl
            );
        }
    }

    public String resolveHttpUrl() {
        try {
            String[] parts = ingestionUrl.split(INGESTION_ENDPOINT);
            if (parts.length < 1) {
                throw new IllegalStateException("Cannot extract base URL from: " + ingestionUrl);
            }
            return parts[0] + INGESTION_ENDPOINT;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve HTTP URL from: " + ingestionUrl, e);
        }
    }

    public SenderType resolveSenderType() {
        try {
            String afterEndpoint = getAfterEndpoint();
            if (afterEndpoint.startsWith("h")) {
                return SenderType.HTTP;
            } else {
                return SenderType.KAFKA;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve sender type from: " + ingestionUrl, e);
        }
    }

    public String resolveBootStrapServer() {
        try {
            String[] parts = ingestionUrl.split(INGESTION_ENDPOINT);
            if (parts.length < 1) {
                throw new IllegalStateException("Cannot extract bootstrap server");
            }

            String server = parts[0];
            if (server.startsWith("kafka://")) {
                server = server.substring("kafka://".length());
            }
            return server;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve bootstrap server from: " + ingestionUrl, e);
        }
    }

    public String resolveTopic() {
        try {
            String afterEndpoint = getAfterEndpoint();
            String[] parts = afterEndpoint.split(CREDENTIALS_DELIMITER);
            if (parts.length < 1 || parts[0].isEmpty()) {
                throw new IllegalStateException("Cannot extract topic name");
            }
            return parts[0];
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve topic from: " + ingestionUrl, e);
        }
    }

    public String resolveAppKey() {
        try {
            String encodedCredentials = getEncodedCredentials();
            String decodedCredentials = decodeBase64(encodedCredentials);
            String[] keys = decodedCredentials.split(KEY_DELIMITER);

            if (keys.length != 2) {
                throw new IllegalStateException(
                        "Invalid credentials format. Expected: {accountKey}-lga-{appKey}, " +
                                "Got: " + decodedCredentials
                );
            }

            String appKey = keys[1];
            if (appKey.isEmpty()) {
                throw new IllegalStateException("AppKey cannot be empty");
            }

            return appKey;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve appKey from: " + ingestionUrl, e);
        }
    }

    public String resolveAccountKey() {
        try {
            String encodedCredentials = getEncodedCredentials();
            String decodedCredentials = decodeBase64(encodedCredentials);
            String[] keys = decodedCredentials.split(KEY_DELIMITER);

            if (keys.length != 2) {
                throw new IllegalStateException(
                        "Invalid credentials format. Expected: {accountKey}-lga-{appKey}, " +
                                "Got: " + decodedCredentials
                );
            }

            String accountKey = keys[0];
            if (accountKey.isEmpty()) {
                throw new IllegalStateException("AccountKey cannot be empty");
            }

            return accountKey;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to resolve accountKey from: " + ingestionUrl, e);
        }
    }

    public String resolveSecret() {
        return secret;
    }

    private String getAfterEndpoint() {
        String fullEndpoint = INGESTION_ENDPOINT + CREDENTIALS_DELIMITER;
        String[] parts = ingestionUrl.split(fullEndpoint);

        if (parts.length < 2) {
            throw new IllegalStateException(
                    "Invalid URL format. Expected: .../api/ingestion/..., " +
                            "Got: " + ingestionUrl
            );
        }

        return parts[1];
    }

    private String getEncodedCredentials() {
        String afterEndpoint = getAfterEndpoint();
        String[] parts = afterEndpoint.split(CREDENTIALS_DELIMITER);

        if (parts.length < 2) {
            throw new IllegalStateException(
                    "Invalid URL format. Missing credentials part. " +
                            "Expected: .../api/ingestion/..., " +
                            "Got: " + ingestionUrl
            );
        }

        String encoded = parts[1];
        if (encoded.isEmpty()) {
            throw new IllegalStateException("Credentials part is empty");
        }

        return encoded;
    }

    /**
     * Decode Base64 string with error handling.
     */
    private String decodeBase64(String encoded) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            return new String(decoded);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Failed to decode credentials. Not valid Base64: " + encoded,
                    e
            );
        }
    }
}
