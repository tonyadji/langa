package com.capricedumardi.agent.core.helpers;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredentialsHelperTest {

    @Test
    void httpCredentialsContainAllExpectedHeaders() {
        CredentialsHelper helper = CredentialsHelper.of("appKey", "accountKey", "secret");

        Map<String, String> credentials = helper.getCredentials(CredentialsHelper.CredentialType.HTTP);

        assertEquals("appKey", credentials.get("X-APP-KEY"));
        assertEquals("accountKey", credentials.get("X-ACCOUNT-KEY"));
        assertTrue(credentials.containsKey("X-USER-AGENT"));
        assertTrue(credentials.containsKey("X-TIMESTAMP"));
        assertTrue(credentials.containsKey("X-AGENT-SIGNATURE"));
    }

    @Test
    void kafkaCredentialsContainAllExpectedFields() {
        CredentialsHelper helper = CredentialsHelper.of("appKey", "accountKey", "secret");

        Map<String, String> credentials = helper.getCredentials(CredentialsHelper.CredentialType.KAFKA);

        assertEquals("appKey", credentials.get("xAppKey"));
        assertEquals("accountKey", credentials.get("xAccountKey"));
        assertTrue(credentials.containsKey("xUserAgent"));
        assertTrue(credentials.containsKey("xTimestamp"));
        assertTrue(credentials.containsKey("xAgentSignature"));
    }

    @Test
    void mqCredentialsAreNotImplemented() {
        CredentialsHelper helper = CredentialsHelper.of("appKey", "accountKey", "secret");

        assertNull(helper.getCredentials(CredentialsHelper.CredentialType.MQ));
    }

    @Test
    void signatureHasNonceHashFormat() {
        CredentialsHelper helper = CredentialsHelper.of("appKey", "accountKey", "secret");

        String signature = helper.getCredentials(CredentialsHelper.CredentialType.HTTP).get("X-AGENT-SIGNATURE");

        String[] parts = signature.split(":");
        assertEquals(2, parts.length);
        assertEquals(32, parts[0].length(), "nonce should be a UUID with dashes stripped");
    }

    @Test
    void eachCallGeneratesAFreshNonceAndSignature() {
        CredentialsHelper helper = CredentialsHelper.of("appKey", "accountKey", "secret");

        String signature1 = helper.getCredentials(CredentialsHelper.CredentialType.HTTP).get("X-AGENT-SIGNATURE");
        String signature2 = helper.getCredentials(CredentialsHelper.CredentialType.HTTP).get("X-AGENT-SIGNATURE");

        assertTrue(!signature1.equals(signature2), "nonce/timestamp should differ between calls, so should the signature");
    }

    @Test
    void differentSecretsProduceDifferentSignatures() {
        CredentialsHelper helperA = CredentialsHelper.of("appKey", "accountKey", "secretA");
        CredentialsHelper helperB = CredentialsHelper.of("appKey", "accountKey", "secretB");

        String sigA = helperA.getCredentials(CredentialsHelper.CredentialType.HTTP).get("X-AGENT-SIGNATURE").split(":")[1];
        String sigB = helperB.getCredentials(CredentialsHelper.CredentialType.HTTP).get("X-AGENT-SIGNATURE").split(":")[1];

        assertTrue(!sigA.equals(sigB));
    }
}
