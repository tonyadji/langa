package com.langa.backend.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyGeneratorTest {

    @Test
    void generateAppKey_shouldStartWithAppPrefix_andNotBeNull() {
        String appKey = KeyGenerator.generateAppKey();
        assertNotNull(appKey);
        assertTrue(appKey.startsWith("APP-"));
        assertTrue(appKey.length() > 4);
    }

    @Test
    void generateAccountKey_shouldStartWithUserPrefix_andBeDeterministic() {
        String email = "user@example.com";
        String accountKey1 = KeyGenerator.generateAccountKey(email);
        String accountKey2 = KeyGenerator.generateAccountKey(email);

        assertNotNull(accountKey1);
        assertTrue(accountKey1.startsWith("U-"));
        assertEquals(accountKey1, accountKey2, "Same email should generate same account key");
    }

    @Test
    void generateAppKey_shouldProduceDifferentKeysEachTime() {
        String key1 = KeyGenerator.generateAppKey();
        String key2 = KeyGenerator.generateAppKey();
        assertNotEquals(key1, key2, "App keys should be unique each time");
    }
}