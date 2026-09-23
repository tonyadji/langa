package com.langa.backend.domain.users;

import com.langa.backend.domain.users.events.ActiveUserRegisteredEvent;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import com.langa.backend.domain.users.valueobjects.UserId;
import com.langa.backend.domain.users.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final ExternalIdentity IDENTITY = new ExternalIdentity("entra", "oid-1", "user@example.com");

    @Test
    void createFromExternalIdentity_shouldCreateActiveLinkedUser() {
        User user = User.createFromExternalIdentity(new ExternalIdentity("cognito", "sub-1", "User@Example.com"));

        assertEquals("user@example.com", user.getEmail());
        assertEquals("cognito", user.getIdentityProvider());
        assertEquals("sub-1", user.getExternalId());
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
        User user = User.createInvited("user@example.com");

        user.linkExternalIdentity(IDENTITY);

        assertEquals("entra", user.getIdentityProvider());
        assertEquals("oid-1", user.getExternalId());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    void linkExternalIdentity_shouldKeepAccountKeyOfLegacyUser() {
        User legacy = User.populate(UserId.of("id-1", "user@example.com", "acc-1"), null, null, UserStatus.ACTIVE);

        legacy.linkExternalIdentity(IDENTITY);

        assertEquals("acc-1", legacy.getAccountKey());
        assertEquals("oid-1", legacy.getExternalId());
    }

    @Test
    void linkExternalIdentity_shouldBeIdempotentForSameIdentity() {
        User user = User.createFromExternalIdentity(IDENTITY);

        assertDoesNotThrow(() -> user.linkExternalIdentity(IDENTITY));
    }

    @Test
    void linkExternalIdentity_shouldRejectAnotherSubject() {
        User user = User.createFromExternalIdentity(IDENTITY);

        assertThrows(UserException.class,
                () -> user.linkExternalIdentity(new ExternalIdentity("entra", "oid-2", "user@example.com")));
    }

    @Test
    void linkExternalIdentity_shouldRejectSameSubjectFromAnotherProvider() {
        User user = User.createFromExternalIdentity(IDENTITY);

        assertThrows(UserException.class,
                () -> user.linkExternalIdentity(new ExternalIdentity("cognito", "oid-1", "user@example.com")));
    }

    @Test
    void linkExternalIdentity_shouldRejectMissingIdentity() {
        User user = User.createInvited("guest@example.com");

        assertThrows(UserException.class, () -> user.linkExternalIdentity(null));
    }

    @Test
    void externalIdentity_shouldRequireAllFields() {
        assertThrows(IllegalArgumentException.class, () -> new ExternalIdentity("entra", " ", "user@example.com"));
        assertThrows(IllegalArgumentException.class, () -> new ExternalIdentity(null, "oid-1", "user@example.com"));
        assertThrows(IllegalArgumentException.class, () -> new ExternalIdentity("entra", "oid-1", null));
    }
}
