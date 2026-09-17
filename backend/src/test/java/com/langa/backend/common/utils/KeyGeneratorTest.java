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

    @Test
    void generateAppSecret_shouldNotBeNull_andBeUniqueEachTime() {
        String secret1 = KeyGenerator.generateAppSecret();
        String secret2 = KeyGenerator.generateAppSecret();

        assertNotNull(secret1);
        assertNotEquals(secret1, secret2);
    }

    @Test
    void generateTeamKey_shouldStartWithTeamPrefix_andBeDeterministic() {
        String key1 = KeyGenerator.generateTeamKey("Dev Team", "owner@example.com");
        String key2 = KeyGenerator.generateTeamKey("Dev Team", "owner@example.com");

        assertTrue(key1.startsWith("T-"));
        assertEquals(key1, key2);
    }

    @Test
    void generateTeamInvitationKey_shouldStartWithInvitationPrefix_andBeDeterministic() {
        String key1 = KeyGenerator.generateTeamInvitationKey("Dev Team", "owner@example.com", "guest@example.com", "2025-01-01");
        String key2 = KeyGenerator.generateTeamInvitationKey("Dev Team", "owner@example.com", "guest@example.com", "2025-01-01");

        assertTrue(key1.startsWith("TI-"));
        assertEquals(key1, key2);
    }

    @Test
    void genericToken_shouldNotBeNull_andBeUniqueEachTime() {
        String token1 = KeyGenerator.genericToken("param1", "param2");
        String token2 = KeyGenerator.genericToken("param1", "param2");

        assertNotNull(token1);
        assertNotEquals(token1, token2, "genericToken embeds a random UUID and timestamp, so it should differ");
    }

    @Test
    void genericToken_shouldWork_withNoParams() {
        assertNotNull(KeyGenerator.genericToken());
    }

    @Test
    void generateIngestionUri_shouldBeBase64Encoded() {
        String uri = KeyGenerator.generateIngestionUri("ACC-1", "APP-1");
        assertNotNull(uri);
        assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(uri));
    }
}