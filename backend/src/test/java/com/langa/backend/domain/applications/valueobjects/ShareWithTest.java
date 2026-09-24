package com.langa.backend.domain.applications.valueobjects;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShareWithTest {

    @Test
    void isCurrentlyActive_shouldBeTrue_whenNoExpirationOrRevocation() {
        ShareWith shareWith = new ShareWith("app-1", "App", "key", SharedWithProfile.USER, LocalDateTime.now(), null, null);
        assertTrue(shareWith.isCurrentlyActive());
        assertFalse(shareWith.isExpired());
        assertFalse(shareWith.isRevoked());
    }

    @Test
    void isExpired_shouldBeTrue_whenExpirationDateInPast() {
        ShareWith shareWith = new ShareWith("app-1", "App", "key", SharedWithProfile.USER,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), null);

        assertTrue(shareWith.isExpired());
        assertFalse(shareWith.isCurrentlyActive());
    }

    @Test
    void isRevoked_shouldBeTrue_whenRevokedDateSet() {
        ShareWith shareWith = new ShareWith("app-1", "App", "key", SharedWithProfile.USER,
                LocalDateTime.now(), null, LocalDateTime.now());

        assertTrue(shareWith.isRevoked());
        assertFalse(shareWith.isCurrentlyActive());
    }
}
