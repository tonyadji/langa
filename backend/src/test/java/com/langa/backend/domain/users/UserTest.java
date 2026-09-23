package com.langa.backend.domain.users;

import com.langa.backend.domain.users.events.ActiveUserRegisteredEvent;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void createFromExternalIdentity_shouldCreateActiveLinkedUser() {
        User user = User.createFromExternalIdentity("oid-1", "user@example.com");

        assertEquals("user@example.com", user.getEmail());
        assertEquals("oid-1", user.getExternalId());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(user.isLinked());
        assertNotNull(user.getAccountKey(), "Account key should be generated");
        assertInstanceOf(ActiveUserRegisteredEvent.class, user.getEvents().getFirst());
    }

    @Test
    void createInvited_shouldCreateUnlinkedUser() {
        User user = User.createInvited("guest@example.com");

        assertEquals(UserStatus.CREATED, user.getStatus());
        assertFalse(user.isLinked());
        assertNotNull(user.getAccountKey());
        assertTrue(user.getEvents().isEmpty());
    }

    @Test
    void linkExternalIdentity_shouldActivateInvitedUser() {
        User user = User.createInvited("guest@example.com");

        user.linkExternalIdentity("oid-1");

        assertEquals("oid-1", user.getExternalId());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void linkExternalIdentity_shouldKeepAccountKeyOfLegacyUser() {
        User legacy = User.populate(
                com.langa.backend.domain.users.valueobjects.UserId.of("id-1", "user@example.com", "acc-1"),
                null, UserStatus.ACTIVE);

        legacy.linkExternalIdentity("oid-1");

        assertEquals("acc-1", legacy.getAccountKey());
        assertEquals("oid-1", legacy.getExternalId());
    }

    @Test
    void linkExternalIdentity_shouldBeIdempotentForSameIdentity() {
        User user = User.createFromExternalIdentity("oid-1", "user@example.com");

        assertDoesNotThrow(() -> user.linkExternalIdentity("oid-1"));
    }

    @Test
    void linkExternalIdentity_shouldRejectAnotherIdentity() {
        User user = User.createFromExternalIdentity("oid-1", "user@example.com");

        assertThrows(UserException.class, () -> user.linkExternalIdentity("oid-2"));
    }

    @Test
    void linkExternalIdentity_shouldRejectBlankIdentity() {
        User user = User.createInvited("guest@example.com");

        assertThrows(UserException.class, () -> user.linkExternalIdentity(" "));
    }
}
