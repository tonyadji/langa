package com.capricedumardi.agent.core.helpers;

import com.capricedumardi.agent.core.config.AgentConfig;
import com.capricedumardi.agent.core.config.ConfigLoader;

import java.util.Map;
import java.util.UUID;

import static com.capricedumardi.agent.core.helpers.HMACUtils.clean;

public class CredentialsHelper {
    private static final String X_USER_AGENT = "X-USER-AGENT";
    private static final String X_AGENT_SIGNATURE = "X-AGENT-SIGNATURE";
    private static final String X_APP_KEY = "X-APP-KEY";
    private static final String X_ACCOUNT_KEY = "X-ACCOUNT-KEY";
    private static final String X_TIMESTAMP = "X-TIMESTAMP";

    private final String appKey;
    private final String accountKey;
    private final String appSecret;
    private static final AgentConfig agentConfig = ConfigLoader.getConfigInstance();

    private CredentialsHelper(String appKey, String accountKey, String appSecret) {
        this.appKey = appKey;
        this.accountKey = accountKey;
        this.appSecret = appSecret;
    }

    public static CredentialsHelper of(String appKey, String accountKey, String appSecret) {
        return new CredentialsHelper(appKey, accountKey, appSecret);
    }

    public Map<String, String> getCredentials(CredentialType credentialType) {
        return switch (credentialType) {
            case HTTP -> buildHttpCredentials();
            case KAFKA -> buildKafkaCredentials();
            case MQ -> null; // call buildMqCredentials()
        };
    }


    private Map<String, String> buildKafkaCredentials() {
        long timestamp = System.currentTimeMillis();
        String signature = buildSignature(appKey, accountKey, timestamp, CredentialType.KAFKA);

        return Map.of(
                "xUserAgent", agentConfig.getAgentVersion(),
                "xAppKey", appKey,
                "xAccountKey", accountKey,
                "xAgentSignature", signature,
                "xTimestamp", String.valueOf(timestamp)
        );
    }

    private Map<String, String> buildHttpCredentials() {
        long timestamp = System.currentTimeMillis();
        String signature = buildSignature(appKey, accountKey, timestamp, CredentialType.HTTP);
        return Map.of(
                X_USER_AGENT, agentConfig.getAgentVersion(),
                X_APP_KEY, appKey,
                X_ACCOUNT_KEY, accountKey,
                X_AGENT_SIGNATURE, signature,
                X_TIMESTAMP, String.valueOf(timestamp)
        );
    }

    private String buildSignature(String appKey, String accountKey, long timestamp, CredentialType credentialType) {
        final String nonce = UUID.randomUUID().toString().replace("-", "");
        String concatMessage = clean(appKey)
                .concat(clean(accountKey))
                .concat(clean(agentConfig.getAgentVersion()))
                .concat(clean(String.valueOf(timestamp)))
                .concat(clean(nonce))
                .concat(credentialType.name());
        return nonce + ":" + HMACUtils.hash(concatMessage, appSecret.trim());
    }

    public enum CredentialType {
        HTTP,
        KAFKA,
        MQ
    }
}
