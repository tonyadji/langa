package com.capricedumardi.agent.core.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class HMACUtilsTest {

    @Test
    void hashIsDeterministicForSameInput() {
        String h1 = HMACUtils.hash("message", "secret");
        String h2 = HMACUtils.hash("message", "secret");

        assertNotNull(h1);
        assertEquals(h1, h2);
    }

    @Test
    void hashDiffersForDifferentMessages() {
        String h1 = HMACUtils.hash("message-a", "secret");
        String h2 = HMACUtils.hash("message-b", "secret");

        assertNotEquals(h1, h2);
    }

    @Test
    void hashDiffersForDifferentSecrets() {
        String h1 = HMACUtils.hash("message", "secret-a");
        String h2 = HMACUtils.hash("message", "secret-b");

        assertNotEquals(h1, h2);
    }

    @Test
    void hashReturnsNullOnEmptySecretKey() {
        // An empty HMAC key is rejected by javax.crypto.spec.SecretKeySpec, which
        // HMACUtils.hash() catches and turns into a null return instead of throwing.
        assertNull(HMACUtils.hash("message", ""));
    }

    @Test
    void cleanTrimsWhitespace() {
        assertEquals("value", HMACUtils.clean("  value  "));
        assertEquals("value", HMACUtils.clean("value"));
    }
}
